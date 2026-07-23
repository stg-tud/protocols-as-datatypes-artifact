package example

import rdts.base.Lattice.syntax
import rdts.base.{Bottom, Lattice, LocalUid, Uid}
import rdts.protocols.Paxos.given
import rdts.protocols.{Participants, Paxos, PaxosRound, Voting}
import rdts.protocols.MultipaxosPhase

import scala.collection.immutable.NumericRange
import rdts.protocols.Util.Agreement
import rdts.protocols.Util.precondition

case class ParallelMultiPaxos[A](
    log: Map[Long, Paxos[A]] = Map.empty[Long, Paxos[A]],
    commitIndex: Long = -1
):
  // private helper functions
  private def currentPaxos: Option[Paxos[A]] = log.get(commitIndex + 1)

  // public API
  def leader(using Participants): Option[Uid] =
    currentPaxos.flatMap(_.currentLeaderElection) match
      case Some(leaderElection) => leaderElection.result
      case None                 => None

  def read(using Participants): List[A] =
    // return values in log order but only if all previous rounds are decided
    NumericRange(0L, log.size.toLong, 1L).view
      .flatMap(log.get)
      // .filter(_.result.isDefined) // TODO: THIS IS A BUG!
      // FIX: return log until first undecided round
      .takeWhile(_.result.isDefined)
      .map(_.result.get)
      .toList

  def startLeaderElection(index: Long)(using LocalUid): ParallelMultiPaxos[A] =
    precondition(
      index == 0L || log.contains(index - 1)
    ) { // index must be 0 or previous index must exist (i.e. no gaps in log allowed)
      val currentPaxos = log.getOrElse(index, Paxos[A]())
      ParallelMultiPaxos(
        Map(index -> currentPaxos.phase1a)
      ) // start new Paxos round with self proposed as leader
    }

  def proposeIfLeader(index: Long, value: A)(using
      LocalUid,
      Participants
  ): ParallelMultiPaxos[A] =
    precondition(
      index == 0L || log.contains(index - 1)
    ) { // index must be 0 or previous index must exist (i.e. no gaps in log allowed)
      def openNextSlot = {
        // opens a new slot for the next log entry, either by reusing the old ballot or starting a new one
        log.get(index - 1).flatMap(_.newestBallotWithLeader) match
          case Some((ballotNum, PaxosRound(leaderElection, _))) =>
            // reuse the old ballot, but empty proposals
            Paxos(rounds =
              Map(
                ballotNum -> PaxosRound(
                  leaderElection = leaderElection,
                  proposals = Voting[A]()
                )
              )
            )
          case None => Paxos[A]()
      }
      val paxos =
        log.getOrElse(index, openNextSlot)

      val paxosVote = paxos.phase2a(value)

      if paxosVote != Paxos() then
        ParallelMultiPaxos(
          Map(
            index -> paxos.merge(paxosVote)
          ) // phase 2a already checks if I am the leader
        )
      else ParallelMultiPaxos()
    }

  def upkeep(using LocalUid, Participants): ParallelMultiPaxos[A] = {
    // perform upkeep in open rounds
    val open = NumericRange(commitIndex + 1, log.size.toLong, 1L).view.map(
      index => (index, log(index))
    )
    val paxosDeltas = open.map { case (index, paxos) =>
      (index, paxos.upkeep())
    }.toMap
    val newLog = log.merge(paxosDeltas)

    // move commit index
    val committed = NumericRange(commitIndex + 1, log.size.toLong, 1L).view
      .flatMap(newLog.get)
      .takeWhile(_.result.isDefined) // return log until first undecided round

    ParallelMultiPaxos(
      log = paxosDeltas,
      commitIndex = commitIndex + committed.size.toLong
    )
  }

  def decision(using Participants): Agreement[List[A]] = read.toList match
    case Nil => Agreement.Undecided
    case xs  => Agreement.Decided(xs)

  override def toString: String =
    lazy val s = s"MultiPaxos(commitIndex: $commitIndex, log: $log)"
    s

object ParallelMultiPaxos:
  def empty[A]: ParallelMultiPaxos[A] = ParallelMultiPaxos[A]()

  given [A]: Lattice[ParallelMultiPaxos[A]] =
    given Lattice[Long] = Math.max
    Lattice.derived

  given [A]: Bottom[ParallelMultiPaxos[A]] = Bottom.provide(empty)
