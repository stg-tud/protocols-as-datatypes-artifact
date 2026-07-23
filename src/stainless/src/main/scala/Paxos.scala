package prdt

// stainless --solvers=smt-z3,smt-cvc5 --timeout=30 --infer-measures=false --check-measures=no --compact=true src/main/scala/{Voting2.scala,Bolton.scala,Util.scala,Helpers.scala, Paxos.scala

import prdt.LocalUid.replicaId
import prdt.Syntax.*
import prdt.Paxos.given
import stainless.lang.Set
import stainless.collection.List
import stainless.collection.Nil
import stainless.collection.Cons
import stainless.collection.ListMap as Map
import stainless.collection.ListMapLemmas._
import stainless.collection.ListOps
import stainless.lang.Option
import stainless.lang.None
import stainless.lang.Some
import scala.Option as ScalaOption
import scala.Some as ScalaSome
import scala.None as ScalaNone
import stainless.annotation._
import scala.collection.immutable.Map as ScalaMap
import scala.collection.immutable.Nil as ScalaNil
import scala.collection.immutable.List as ScalaList
import scala.Option as ScalaOption
import scala.unchecked
import stainless.lang.Some
import stainless.lang.None
import stainless.lang.BooleanDecorations
import stainless.proof._
import stainless.lang.because
import prdt.Paxos.PaxosAction

type LeaderElection = Voting[Uid]
type ParticipantList = List[Uid]

case class BallotNum(uid: Uid, counter: Long)

case class PaxosRound[A](
    leaderElection: LeaderElection,
    proposals: Voting[A]
)
object PaxosRoundMerge {
  def merge[A](p1: PaxosRound[A], p2: PaxosRoundDelta[A]): PaxosRound[A] = {
    val leaderElection = Voting.merge(p1.leaderElection, p2.leaderElection)
    val proposals = Voting.merge(p1.proposals, p2.proposals)
    PaxosRound(leaderElection, proposals)
  }.ensuring((res: PaxosRound[A]) =>
    (res.leaderElection == Voting
      .merge(p1.leaderElection, p2.leaderElection)) &&
      (res.proposals == Voting.merge(p1.proposals, p2.proposals))
  )
}

case class PaxosDelta[A](
    rounds: Map[BallotNum, PaxosRoundDelta[A]] =
      Map.empty[BallotNum, PaxosRoundDelta[A]]
)

case class PaxosRoundDelta[A](
    leaderElection: VotingDelta[Uid] = VotingDelta.empty[Uid],
    proposals: VotingDelta[A] = VotingDelta.empty[A]
)

case class Paxos[A](
    rounds: Map[BallotNum, PaxosRound[A]] = Map.empty[BallotNum, PaxosRound[A]],
    participants: ParticipantList
) {
  // voting
  def voteLeader(round: BallotNum, leader: Uid)(using
      LocalUid
  ): (BallotNum, PaxosRoundDelta[A]) = {
    (round ->
      PaxosRoundDelta(leaderElection =
        rounds
          .get(round)
          .getOrElse(PaxosRound(Voting(participants), Voting(participants)))
          .leaderElection
          .voteFor(leader)
      ))
  }.ensuring((res: (BallotNum, PaxosRoundDelta[A])) =>
    res._2.proposals.votesMap.isEmpty
  )
  def voteValue(round: BallotNum, value: A)(using
      LocalUid
  ): (BallotNum, PaxosRoundDelta[A]) = {
    (round ->
      PaxosRoundDelta(proposals =
        rounds
          .get(round)
          .getOrElse(PaxosRound(Voting(participants), Voting(participants)))
          .proposals
          .voteFor(value)
      ))
  }

  // protocol actions:
  def phase1aPrecond(round: BallotNum)(using LocalUid): Boolean = {
    round.uid == replicaId
  }.ensuring(res => res == (round.uid == replicaId))

  def phase1a(nextBallotNum: BallotNum)(using LocalUid): PaxosDelta[A] = {
    precondition(phase1aPrecond(nextBallotNum)) {
      val deltaRounds = List(voteLeader(nextBallotNum, replicaId))
      PaxosDelta(
        Map(deltaRounds)
      ) // try to become leader
    }
  }.ensuring((res: PaxosDelta[A]) =>
    phase1aPrecond(nextBallotNum) ==> ((res == PaxosDelta(
      Map(List(voteLeader(nextBallotNum, replicaId)))
    )))
  )

  def phase1bPrecond(
      round: BallotNum,
      candidate: Uid
  )(using LocalUid): Boolean = {
    // check if given round has candidate
    rounds.get(round) match
      case Some(r) =>
        // FIXME: somehow stainless doesn't accept this if written in one line
        val leaderElection = r.leaderElection
        leaderElection.votesMap.values.contains(candidate)
      case _ => false
  }
    .ensuring(res =>
      res == (
        rounds.contains(round) && {
          val le = rounds(round).leaderElection
          le.votesMap.values.contains(candidate)
        }
      )
    )

  def phase1b(
      round: BallotNum,
      candidate: Uid
  )(using LocalUid, Bottom[PaxosDelta[A]]): PaxosDelta[A] =
    precondition(phase1bPrecond(round, candidate)) {
      // vote in the current leader election, no need to include previously voted for values because we assume causal delivery
      PaxosDelta(
        Map.empty + (voteLeader(round, candidate))
      )
    }.ensuring((res: PaxosDelta[A]) =>
      phase1bPrecond(round, candidate) ==> (
        (res == PaxosDelta(Map.empty + (voteLeader(round, candidate)))) &&
          (res.rounds.contains(round)) &&
          (res.rounds.size == 1) &&
          (res.rounds(round).proposals.votesMap.isEmpty)
      )
    )

  def phase2aPrecond(round: BallotNum)(using LocalUid): Boolean = {
    rounds.get(round) match
      case Some(r) =>
        // only the leader can start the value voting by proposing a value
        (r.leaderElection.decision == Decision.Decided(replicaId)) &&
        r.proposals.votesMap.isEmpty
      case _ => false
  }.ensuring(res =>
    res == (
      rounds.contains(round) &&
        (rounds(round).leaderElection.decision == Decision.Decided(
          replicaId
        )) &&
        rounds(round).proposals.votesMap.isEmpty
    )
  )

  def phase2a(
      round: BallotNum,
      value: A
  )(using LocalUid, Bottom[PaxosDelta[A]]): PaxosDelta[A] = {
    // propose a value if I am the leader for the current round
    precondition(phase2aPrecond(round))(
      // propose most recent received value or my value if there is none
      newestReceivedVal match // TODO: this should actually be bound by a precondition that compares ballotNums
        case Some(v) =>
          PaxosDelta(
            Map.empty + (
              voteValue(round, v)
            )
          )
        case None() =>
          PaxosDelta(
            Map.empty + (
              voteValue(round, myValue)
            )
          )
    )
  }.ensuring((res: PaxosDelta[A]) =>
    phase2aPrecond(round) ==> (
      ((res == PaxosDelta(
        Map.empty + (
          voteValue(round, myValue)
        )
      )) || (newestReceivedVal.nonEmpty && (res == PaxosDelta(
        Map.empty + (
          voteValue(round, newestReceivedVal.get)
        )
      )))) &&
        (res.rounds.size == 1) &&
        (res.rounds.contains(round)) &&
        (res.rounds(round).leaderElection.votesMap.isEmpty)
    )
  )

  def phase2bPrecond(
      round: BallotNum,
      value: A
  )(using LocalUid): Boolean = {
    rounds.get(round) match
      case Some(r) =>
        // check if the value we are voting for was proposed
        r.proposals.votesMap.values.contains(value)
      case _ => false
  }.ensuring((res) =>
    (
      rounds.contains(round) &&
        rounds(round).proposals.votesMap.values.contains(value)
    ) == res
  )

  def phase2b(
      round: BallotNum,
      value: A
  )(using LocalUid, Bottom[PaxosDelta[A]]): PaxosDelta[A] = {
    // accept proposed value
    precondition(phase2bPrecond(round, value)) {
      PaxosDelta(Map.empty + ((voteValue(round, value))))
    }
  }.ensuring((res: PaxosDelta[A]) =>
    phase2bPrecond(round, value) ==> (
      (res == PaxosDelta(
        Map.empty + (voteValue(round, value))
      ) && res.rounds.size == 1
        && res.rounds.contains(round)
        && (res.rounds(round).proposals.votesMap.nonEmpty ==>
          ((res.rounds(round).proposals.votesMap.values.head == value))))
    )
  )

  // // decision function
  def decision: Decision[A] = {
    currentRound match
      case Some(ballot) =>
        // check if the current round has a decision
        decisionPerRound(ballot) match
          case d @ Decision.Decided(value) => d
          // if not, check if a previous round has a decision
          case Decision.Undecided() =>
            previousRoundWithDecision.map(_._2) match
              case Some(v) => v.decision
              case None()  => Decision.Undecided()
      case None() => Decision.Undecided()
  }

  def decisionPerRound(round: BallotNum): Decision[A] = {
    require(rounds.contains(round))
    rounds(round).proposals.decision
  }

  // helper functions
  @extern
  def currentRound: Option[BallotNum] = {
    rounds.toList.toScala.maxByOption(_._1).map(_._1) match
      case ScalaSome(value) => Some(value)
      case ScalaNone        => None()

  }.ensuring((res: Option[BallotNum]) =>
    res.nonEmpty ==> rounds.contains(res.get)
  )

  // returns the last round where the local process accepted something
  @extern
  def lastRoundWithAcceptedVote(using
      LocalUid
  ): ScalaOption[(BallotNum, PaxosRound[A])] = {
    val scalaOpt = rounds.toList
      .filter { case (ballot, PaxosRound(leaderElection, proposals)) =>
        proposals.votesMap.nonEmpty &&
        proposals.votesMap.contains(replicaId)
      }
      .toScala
      .maxByOption(_._1)
    scalaOpt
  }

  // returns last accepted value
  @extern
  def lastAcceptedVal(using
      LocalUid
  ): Option[(BallotNum, A)] = {
    lastRoundWithAcceptedVote match
      case ScalaSome(ballot, round) =>
        Some(ballot, round.proposals.votesMap.values.head)
      case ScalaNone => None()
  }

  // @extern
  def previousRoundWithDecision: Option[(BallotNum, Voting[A])] = {
    rounds.toList
      // TODO: sort by BallotNum
      .filter { (b, r) =>
        r.proposals.decision != Decision.Undecided()
      }
      .lastOption
      .map((b, p) => (b, p.proposals))
  }

  @extern
  def newestReceivedVal(using LocalUid): Option[A] =
    rounds.toList.toScala
      .filter(_._2.proposals.votesMap.nonEmpty) // filter rounds with proposal
      .maxByOption(_._1) // get newest
      .map(_._2.proposals.votesMap.head._2) // extract proposal
    // TODO: sort by BallotNum
    match
      case ScalaSome(value) => Some(value)
      case ScalaNone        => None()

  // we store each processes starting value in a magic ballot with id -1
  @extern
  def myValue(using LocalUid): A =
    val magicBallot = BallotNum(replicaId, -1)
    // require(rounds.get(magicBallot).nonEmpty)
    // require(rounds(magicBallot).proposals.votesMap.nonEmpty)
    rounds(magicBallot).proposals.votesMap.toList.head._2
}

object Paxos {

  @extern
  given ballotOrdering: Ordering[BallotNum] =
    new scala.math.Ordering[BallotNum] {
      override def compare(x: BallotNum, y: BallotNum): Int =
        if x.counter > y.counter then 1
        else if x.counter < y.counter then -1
        else Ordering[Uid].compare(x.uid, y.uid)
    }

  // @extern
  def merge[A](p1: Paxos[A], p2: PaxosDelta[A]): Paxos[A] = {
    val participants = p1.participants
    p2.rounds.toList match
      case Cons((ballot, roundDelta), t) =>
        val p1round = p1.rounds
          .get(ballot)
          .getOrElse(
            PaxosRound(Voting(participants), Voting(participants))
          )
        val mergedRound = PaxosRoundMerge.merge(p1round, roundDelta)
        val updated =
          Paxos(p1.rounds.updated(ballot, mergedRound), participants)
        merge(updated, PaxosDelta(rounds = Map(t)))
      case Nil() => p1
  }

  enum PaxosAction[A] {
    case phase1a(round: BallotNum, voter: LocalUid)
    case phase1b(
        round: BallotNum,
        voter: LocalUid,
        candidate: Uid
    )
    case phase2a(round: BallotNum, leader: LocalUid, value: A)
    case phase2b(round: BallotNum, voter: LocalUid, value: A)
  }

  given PaxosProof[A]: Consensus[Paxos[A]] with {
    type Action = PaxosAction[A]
    type Delta = PaxosDelta[A]

    def merge(s1: State, s2: Delta): State =
      Paxos.merge(s1, s2)

    def apply(s: Paxos[A], a: Action, i: Id): Delta =
      a match
        case PaxosAction.phase1a(round, voter) =>
          s.phase1a(round)(using voter)
        case PaxosAction.phase1b(round, voter, candidate) =>
          s.phase1b(round, candidate)(using
            voter
          )
        case PaxosAction.phase2a(round, voter, value) =>
          s.phase2a(round, value)(using voter)
        case PaxosAction.phase2b(round, voter, value) =>
          s.phase2b(round, value)(using voter)

    def precond(s: State, a: Action, i: Id): Boolean =
      a match
        case PaxosAction.phase1a(round, voter) =>
          s.phase1aPrecond(round)(using voter) && i == voter
        case PaxosAction.phase1b(round, voter, candidate) =>
          s.phase1bPrecond(round, candidate)(using
            voter
          ) && i == voter
        case PaxosAction.phase2a(round, voter, value) =>
          s.phase2aPrecond(round)(using voter) && i == voter
        case PaxosAction.phase2b(round, voter, value) =>
          s.phase2bPrecond(round, value)(using voter) && i == voter

    def decisionOrder(s1: Paxos[A], s2: Paxos[A]): Boolean =
      (s1.decision == Decision.Undecided()) ||
        (s1.decision == s2.decision)

    @pure @ghost
    override def stability(
        n1: Id,
        n2: Id,
        e1: Action,
        e2: Action,
        s: State
    ): Boolean = {
      // apply a2
      val delta: Delta = apply(s, e2, n2)
      val s1 = merge(s, delta)
      (n1 != n2 &&
        precond(s, e1, n1) &&
        precond(s, e2, n2)) ==>
        (precond(s1, e1, n1) because {
          e1 match
            case PaxosAction.phase1a(round, voter) =>
              trivial
            case PaxosAction.phase1b(round, voter, candidate) =>
              PaxosLemmas.mergeRetainsLocallyLastAcceptedVal(s, delta, n1)
              PaxosLemmas.mergeRetainsLeaderVotes(s, delta, round, candidate)
            case PaxosAction.phase2a(round, leader, value) =>
              assert(precond(s, PaxosAction.phase2a(round, leader, value), n1))
              assert(n1 == leader)
              assert(s.rounds.contains(round))
              assert(
                s.rounds(round).leaderElection.decision == Decision.Decided(
                  leader.uid
                )
              )
              PaxosLemmas.mergeRetainsLeaderElectionDecisions(
                s,
                delta,
                round,
                leader.uid
              )
              PaxosLemmas.onlyLeaderCanStartVotingRound(
                s,
                delta,
                round,
                leader,
                n2,
                e2
              )
            case PaxosAction.phase2b(round, voter, value) =>
              PaxosLemmas.mergeRetainsExistingProposals(
                s,
                delta,
                round,
                value
              )
        })
    }

    @pure @ghost
    override def monotonicity(
        n: Id,
        e: Action,
        s: State
    ): Boolean = {
      val delta = apply(s, e, n)
      val s1 = merge(s, delta)
      precond(s, e, n) ==> (decisionOrder(s, s1) because {
        e match
          case PaxosAction.phase1a(round, voter) =>
            PaxosLemmas.votingInLeaderElectionNeverChangesDecision(
              s,
              delta,
              round
            )
            trivial
          case PaxosAction.phase1b(round, voter, candidate) =>
            PaxosLemmas.votingInLeaderElectionNeverChangesDecision(
              s,
              delta,
              round
            )
          case PaxosAction.phase2a(round, voter, value) =>
            PaxosLemmas.startingNewVotingRoundCantChangeDecision(
              s,
              delta,
              round
            )
          case PaxosAction.phase2b(round, voter, value) =>
            // this is a vote for a running proposal
            // this means that the proposed value is the same as the decision
            // we are voting on the proposed value
            // voting for the decided value cant change the decision
            s.decision match
              case Decision.Decided(decision) =>
                // there is a decision already
                // this means, that the leader who proposed this value has received the decision from at least one of the peers that elected it as leader
                // check if this vote does something
                if (delta.rounds(round).proposals.votesMap.isEmpty) then
                  PaxosLemmas.votingInLeaderElectionNeverChangesDecision(
                    s,
                    delta,
                    round
                  )
                else
                  PaxosLemmas.canOnlyProposeDecisions(s, round, decision)
                  PaxosLemmas.containsAndForallImpliesEquals(
                    s.rounds(round).proposals.votesMap.values,
                    value,
                    decision
                  )
                  PaxosLemmas.votingForDecisionCantChangeDecision(
                    s,
                    delta,
                    round,
                    decision
                  )
              case Decision.Undecided() =>
                trivial
            true
      })
    }
  }

  given [A]: Bottom[PaxosDelta[A]] with
    def empty: PaxosDelta[A] = PaxosDelta()
}

object PaxosLemmas {

  // merging does not remove existing proposals
  @dropVCs
  @pure
  def mergeRetainsExistingProposals[A](
      p1: Paxos[A],
      p2: PaxosDelta[A],
      b: BallotNum,
      v: A
  ): Boolean = {
    require(p1.rounds.contains(b))
    require(p1.rounds(b).proposals.votesMap.values.contains(v))
    val m = Paxos.merge(p1, p2)

    m.rounds.contains(b) &&
    m.rounds(b).proposals.votesMap.values.contains(v)
  }.holds

  // merging does not remove leader election votes
  @dropVCs
  @pure
  def mergeRetainsLeaderVotes[A](
      p1: Paxos[A],
      p2: PaxosDelta[A],
      b: BallotNum,
      candidate: Uid
  ): Boolean = {
    require(p1.rounds.contains(b))
    val leaderElection = p1.rounds(b).leaderElection
    require(leaderElection.votesMap.values.contains(candidate))
    val m = Paxos.merge(p1, p2)

    m.rounds.contains(b)
    && {
      val m1LeaderElection = m.rounds(b).leaderElection
      m1LeaderElection.votesMap.values.contains(candidate)
    }
  }.holds

  // merging does not change which value the local process last accepted
  @dropVCs
  @pure
  def mergeRetainsLocallyLastAcceptedVal[A](
      p1: Paxos[A],
      p2: PaxosDelta[A],
      localUid: LocalUid
  ): Boolean = {
    val m = Paxos.merge(p1, p2)

    m.lastAcceptedVal(using localUid) == p1.lastAcceptedVal(using localUid)
  }.holds

  // merging can not change already decided leader elections. This is true due to monotonicity of voting
  @dropVCs
  @pure
  def mergeRetainsLeaderElectionDecisions[A](
      p1: Paxos[A],
      p2: PaxosDelta[A],
      round: BallotNum,
      leader: Uid
  ): Boolean = {
    require(p1.rounds.contains(round))
    require(
      p1.rounds(round).leaderElection.decision == Decision.Decided(leader)
    )
    val m = Paxos.merge(p1, p2)

    m.rounds.contains(round) && m.rounds(round).leaderElection.decision == p1
      .rounds(round)
      .leaderElection
      .decision
  }.holds

  // only adding a vote to a leader election can never change the decision because the decision only depends on votes on proposals
  @dropVCs
  @pure
  def votingInLeaderElectionNeverChangesDecision[A](
      p1: Paxos[A],
      p2: PaxosDelta[A],
      round: BallotNum
  ): Boolean = {
    require(p2.rounds.size == 1)
    require(p2.rounds.contains(round))
    val proposals = p2.rounds(round).proposals
    require(proposals.votesMap.isEmpty)
    val m = Paxos.merge(p1, p2)

    p1.decision == m.decision
  }.holds

  // kicking off a new round of voting can't change the decision
  @pure
  @dropVCs
  def startingNewVotingRoundCantChangeDecision[A](
      p1: Paxos[A],
      p2: PaxosDelta[A],
      round: BallotNum
  ): Boolean = {
    // this is a vote for a value, not a leader election
    require(p2.rounds.size == 1)
    require(p2.rounds.contains(round))
    require(p2.rounds(round).leaderElection.votesMap.isEmpty)
    // it is the first vote for a value in this round
    require(p1.rounds.contains(round))
    require(p1.rounds(round).proposals.votesMap.isEmpty)

    val m = Paxos.merge(p1, p2)
    p1.decision == m.decision
  }.holds

  // if the proposals voting for a round wasn't started yet, only the leader can start it and it remains empty otherwise
  @pure
  @dropVCs
  def onlyLeaderCanStartVotingRound[A](
      p1: Paxos[A],
      p2: PaxosDelta[A],
      round: BallotNum,
      leader: LocalUid,
      other: LocalUid,
      action: PaxosAction[A]
  ): Boolean = {
    // leader is leader in p1
    require(p1.rounds.contains(round))
    require(p1.rounds(round).proposals.votesMap.isEmpty)
    require(
      p1.rounds(round).leaderElection.decision == Decision.Decided(leader.uid)
    )

    // this is a vote not by the leader
    require(p2 == PaxosProof.apply(p1, action, other))
    require(leader != other)

    val m = Paxos.merge(p1, p2)
    m.rounds.contains(round) &&
    (m.rounds(round).proposals == p1.rounds(round).proposals)
  }.holds

  // voting for an already decided value can't change anything
  @pure
  @dropVCs
  def votingForDecisionCantChangeDecision[A](
      p1: Paxos[A],
      p2: PaxosDelta[A],
      round: BallotNum,
      decision: A
  ): Boolean = {
    // there is a decision
    require(p1.decision == Decision.Decided(decision))

    // delta is a vote for that decision
    require(p2.rounds.size == 1)
    require(p2.rounds.contains(round))
    require(p2.rounds(round).proposals.votesMap.nonEmpty)
    require(p2.rounds(round).proposals.votesMap.values.head == decision)

    val m = Paxos.merge(p1, p2)
    (m.decision == p1.decision)
  }.holds

  // if there is a previous decision, the leader must have learned that value when it was elected, therefore it must propose that value again
  @pure
  @dropVCs
  def canOnlyProposeDecisions[A](
      p1: Paxos[A],
      round: BallotNum,
      decision: A
  ): Boolean = {
    // there is a decision
    require(p1.decision == Decision.Decided(decision))
    require(p1.rounds.contains(round))

    // then the leader can have only proposed the decision again
    p1.rounds(round).proposals.votesMap.values.forall(_ == decision)
  }.holds

  // if every element in a list is "a", then any contained element is "a"
  @pure
  @dropVCs
  def containsAndForallImpliesEquals[A](
      l: List[A],
      a: A,
      b: A
  ): Boolean = {
    require(l.contains(a))
    require(l.forall(_ == b))

    a == b
  }.holds
}
