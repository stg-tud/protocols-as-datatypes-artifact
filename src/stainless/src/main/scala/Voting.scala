package prdt
import prdt.LocalUid.replicaId
import prdt.LocalUid
import prdt.Uid
import prdt.Id

// stainless command:
// ./stainless --solvers=smt-z3,smt-cvc5 --timeout=30 --watch ../src/main/scala/*

// scala standards renaming
import scala.collection.immutable.Map as ScalaMap
import scala.collection.immutable.Nil as ScalaNil
import scala.collection.immutable.List as ScalaList
import scala.Option as ScalaOption
import scala.unchecked

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
import stainless.lang.BooleanDecorations
import stainless.lang.decreases
import stainless.annotation._
import stainless.lang.StaticChecks._
import stainless.proof._
import stainless.proof.check
import prdt.Helpers.*
import stainless.lang.because

enum VotingEffect[A]:
  case voteFor(voter: LocalUid, a: A)

@inlineInvariant
case class VotingDelta[A](votesMap: Map[Uid, A]) {
  require(votesMap.size == 1 || votesMap.size == 0)
}

object VotingDelta {
  def empty[A]: VotingDelta[A] = VotingDelta(votesMap = Map.empty[Uid, A])

  def fromVoting[A](v: Voting[A]): VotingDelta[A] =
    require(v.votesMap.size == 1 || v.votesMap.size == 0)
    VotingDelta(v.votesMap)

}

case class Voting[A](
    participants: List[Uid],
    votesMap: Map[Uid, A] = Map.empty[Uid, A]
) {
  def threshold: BigInt = participants.size / 2

  def result: Option[A] = {
    leadingCount match
      case Some((v, count)) if count > threshold => Some(v)
      case _                                     => None()
  }
    .ensuring((res: Option[A]) =>
      (votesMap.isEmpty ==> res.isEmpty) &&
        ((leadingCount.nonEmpty && leadingCount.get._2 > threshold) ==> (res == Some[
          A
        ](leadingCount.get._1))) &&
        (res.nonEmpty ==> (res == Some[A](leadingCount.get._1)))
    )

  def decision: Decision[A] = {
    result match
      case Some(v) => Decision.Decided(v)
      case None()  => Decision.Undecided()
  }

  def precond(using LocalUid): Boolean =
    participants.contains(replicaId) && !votesMap.contains(replicaId)

  def voteFor(v: A)(using LocalUid): VotingDelta[A] = {
    if participants.contains(replicaId) && !votesMap.contains(replicaId) then
      VotingDelta(Map.empty + (replicaId -> v))
    else VotingDelta(Map.empty)
  }.ensuring { (res: VotingDelta[A]) =>
    // (!participants.contains(replicaId) ==> res.votesMap.isEmpty) &&
    (!votesMap.contains(replicaId) && participants.contains(replicaId)) ==> (
      res.votesMap.toList == List((replicaId, v)) &&
        res.votesMap(replicaId) == v &&
        res.votesMap.size == 1 &&
        res.votesMap.keys.head == replicaId
    )
  }

  @pure
  def countVotes(candidate: A): BigInt = {
    votesMap.toList.filter(_._2 == candidate).size
  }.ensuring((res: BigInt) =>
    res == votesMap.toList.filter(_._2 == candidate).size &&
      (!votesMap.toList.map(_._2).contains(candidate) ==> (res == 0)) because {
        if !votesMap.toList.map(_._2).contains(candidate) then
          Lemmas.countingNonexistent(votesMap.toList, candidate)
        else trivial
      }
  )

  @extern
  def maxByVotes: A = {
    require(votesMap.nonEmpty)
    votesMap.toList.toScala
      .groupBy(_._2)
      .map((a, v) => (a, v.size))
      .maxBy(_._2)
      ._1
  }.ensuring((res: A) =>
    val options = votesMap.toList
    options.map(_._2).contains(res) && // is valid option
    // result actually has biggest count
    ((options: @unchecked) match
      case Cons((_, candidate), t @ Cons(_, _)) =>
        votesMap.values.forall(candidate =>
          (countVotes(res) >= countVotes(candidate)) &&
            ((countVotes(candidate) > threshold) ==> (res == candidate)) &&
            ((res == candidate) ==> (countVotes(candidate) > threshold))
        )
      case Cons((_, candidate), Nil()) => res == candidate
    )
  )

  // variant for verification purposes
  @extern @ghost
  def maxByVotes(witness: A): A = {
    require(votesMap.nonEmpty)
    maxByVotes
  }.ensuring((res: A) =>
    res == maxByVotes &&
      // if the witness has more votes than the threshold, it has to be the result
      ((countVotes(witness) > threshold) ==> (res == witness))
  )

  @pure
  def leadingCount: Option[(A, BigInt)] = {
    if votesMap.toList.nonEmpty then
      val max = maxByVotes
      Some(max, countVotes(max))
    else None()
  }.ensuring((res: Option[(A, BigInt)]) =>
    res match
      case Some((leader, leaderCount)) =>
        leaderCount == countVotes(maxByVotes)
      case None() => votesMap.isEmpty
  )
}

given votingPRDTProof[A]: Consensus[Voting[A]] with {
  type Action = VotingEffect[A]
  type Delta = VotingDelta[A]

  def apply(s: State, a: Action, i: Id): Delta =
    a match
      case VotingEffect.voteFor(voter, a) => s.voteFor(a)(using voter)

  def merge(s1: State, s2: Delta): State = Voting.merge(s1, s2)

  def precond(s: Voting[A], a: Action, i: Id): Boolean =
    a match
      case VotingEffect.voteFor(voter, a) => s.precond(using i) && voter == i

  def decisionOrder(s1: Voting[A], s2: Voting[A]): Boolean =
    s1.result match
      case None()  => true
      case Some(_) => s1.result == s2.result

  @pure @ghost
  override def monotonicity(
      n: Id,
      e: Action,
      s: Voting[A]
  ): Boolean =
    val delta = apply(s, e, n)
    val s1 = merge(s, delta)
    precond(s, e, n) ==> decisionOrder(s, s1) because {
      if !precond(s, e, n) then
        check(precond(s, e, n) ==> decisionOrder(s, s1))
        true
      else
        check(precond(s, e, n))
        if delta.votesMap.isEmpty then
          check(s == s1)
          check(decisionOrder(s, s1))
          check(precond(s, e, n) ==> decisionOrder(s, s1))
          true
        else
          s.result match
            case Some(v) =>
              check(
                !s.votesMap.contains(delta.votesMap.head._1)
              ) // is new vote
              Lemmas.addingVoteCantChangeExistingDecision(s, v, e, n)
              check(decisionOrder(s, s1))
              check(precond(s, e, n) ==> decisionOrder(s, s1))
              true
            case None() =>
              check(decisionOrder(s, s1))
              check(precond(s, e, n) ==> decisionOrder(s, s1))
              true
    }

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
      ((precond(s1, e1, n1)) because {
        (e1, e2) match
          case (
                VotingEffect.voteFor(voter1, cand1),
                VotingEffect.voteFor(voter2, cand2)
              ) =>
            Lemmas.addingToMapsOnlyAddsOne(
              s.votesMap,
              (voter2.uid, cand2),
              voter1.uid
            )
            check(s1.participants.contains(voter1.uid))
            check(!s1.votesMap.contains(voter1.uid))
            check(voter1 == n1)
            check(e1 == VotingEffect.voteFor(voter1, cand1))
            check(precond(s1, e1, n1) == (s1.precond(using n1) && voter1 == n1))
            check(precond(s1, e1, n1))

            true
      })
  }

}

@ghost
object Lemmas {

  def containsImpliesNonEmpty[A](l: List[A], el: A): Boolean = {
    require(l.contains(el))
    l.nonEmpty
  }.holds

  @ghost @pure
  def countingNonexistent[A](l: List[(Uid, A)], el: A): Boolean = {
    require(!l.map(_._2).contains(el))
    l.filter(_._2 == el).size == 0 because {
      l match
        case Cons((_, e), t) =>
          countingNonexistent(t, el)
          e != el
        case Nil() => trivial
    }
  }.holds

  @ghost @pure
  def gtTransitive(
      a: BigInt,
      b: BigInt,
      l: List[BigInt]
  ): Boolean = {
    require(a >= b)
    require(l.forall(b >= _))

    l.forall(a >= _) because {
      l match
        case Cons(h, t) =>
          assert(a >= h)
          gtTransitive(a, b, t)
        case Nil() => trivial
    }
  }.holds

  @ghost @pure
  def gtTransitiveAppend(
      a: BigInt,
      b: BigInt,
      l: List[BigInt]
  ): Boolean = {
    require(a >= b)
    require(l.forall(a >= _))

    (b :: l).forall(a >= _) because {
      l match
        case Cons(h, t) =>
          assert(a >= h)
          gtTransitiveAppend(a, b, t)
        case Nil() => trivial
    }
  }.holds

  @ghost @pure
  def gtEquality(
      a: BigInt,
      b: BigInt,
      l: List[BigInt]
  ): Boolean = {
    require(a == b)
    require(l.forall(a >= _))

    l.forall(b >= _)
  }.holds

  @ghost @pure
  def addingVoteCantChangeExistingDecision[A](
      s: Voting[A],
      decision: A,
      e: VotingEffect[A],
      n: LocalUid
  ): Boolean = {
    require(s.precond(using n))
    // require(votingIsConsensus.sup(n, e, s))
    val delta = e.match
      case VotingEffect.voteFor(voter, a) => s.voteFor(a)(using voter)
    require(!delta.votesMap.isEmpty)
    require(!s.votesMap.contains(delta.votesMap.head._1)) // is new vote

    (s.result == Some[A](decision) ==>
      (Voting.merge(s, delta).result == Some[A](decision))) because {
      if s.result != Some[A](decision) then trivial
      else
        majorityStaysMajority(s, delta)
        Voting.merge(s, delta).leadingCount.get._2 > s.threshold
    }
  }.holds

  @ghost @pure
  def majorityStaysMajority[A](
      s: Voting[A],
      e: VotingDelta[A]
  ): Boolean = {
    require(s.leadingCount.nonEmpty)
    require(s.leadingCount.get._2 > s.threshold)
    require(!e.votesMap.isEmpty)
    require(
      !s.votesMap.contains(e.votesMap.head._1)
    ) // is new vote

    val s1 = Voting.merge(s, e)
    val (oldLead, oldLeadCount) = s.leadingCount.get
    val (_, candidate) = e.votesMap.head
    val (newLead, newLeadCount) = s1.leadingCount.get

    ((oldLead == newLead) && // lead does not change after threshold
      (newLeadCount >= oldLeadCount)) because { // count can only increase

      // relevant lemmas
      // adding a new element to a map keeps all old elements:
      addNewElemToMap(s.votesMap, e.votesMap.head)

      // the old lead had a majority
      check(s1.countVotes(oldLead) > (s1.participants.size / 2))
      check(s1.countVotes(oldLead) > (s1.threshold))

      check(
        (s1.maxByVotes(oldLead) == oldLead)
      )

      check(
        (s1.maxByVotes == oldLead)
      )
      true
    }
  }.holds

  def canOnlyVoteOnce(participants: List[Uid])(using LocalUid): Unit = {
    require(participants.size > 0)
    val v = Voting[BigInt](participants = participants)
    require(participants.contains(replicaId))
    val step1 = Voting.merge(v, v.voteFor(1))
    assert(step1.votesMap(replicaId) == 1)
    // val step2 = Voting.merge(step1, step1.voteFor(2)) // this should fail
  }

  @pure @ghost
  def addingToMapsOnlyAddsOne[A](
      m: Map[Uid, A],
      u: (Uid, A),
      i: Uid
  ): Boolean = {
    decreases(m)
    require(u._1 != i)
    require(!m.contains(i))
    val updated = m + u
    !updated.contains(i) because {
      m.toList match
        case Nil() => trivial
        case Cons(x, xs) =>
          assert(x._1 != i)
          addingToMapsOnlyAddsOne(Map(xs), u, i)
    }
  }.holds

  // addint to a map keeps the old entries
  @ghost @pure
  def addingToMapsKeepsOld[A](m: Map[Uid, A], u: (Uid, A), i: Uid): Boolean = {
    decreases(m)
    require(m.contains(i))
    val updated = m + u
    (updated.contains(i) because {
      (m.toList: @unchecked) match
        case Cons(x, xs) =>
          if x._1 == i then trivial
          else addingToMapsKeepsOld(Map(xs), u, i)
    }) &&
    (u._1 != i) ==> (updated(i) == m(i))
  }.holds

  def addingToMapAdds[A](m: Map[Uid, A], u: (Uid, A)): Boolean = {
    val updated = m + u
    updated.contains(u._1)
  }.holds

  @ghost @pure
  def addNewElemToMap[A, B](m: Map[A, B], el: (A, B)): Boolean = {
    decreases(m)
    require(!m.contains(el._1))

    val m1 = m + el

    m1.toList == el :: m.toList because {
      m.toList match
        case Cons(x, xs) =>
          addNewElemToMap(Map(xs), el)
        case Nil() => trivial
    }
  }.holds

}

object Voting {

  def merge[A](v1: Voting[A], v2: VotingDelta[A]): Voting[A] = {
    require(v2.votesMap.size == 1 || v2.votesMap.size == 0)
    if v2.votesMap.isEmpty then v1
    else
      Voting(
        votesMap = v1.votesMap + v2.votesMap.toList.head,
        participants = v1.participants
      )
  }.ensuring((res: Voting[A]) =>
    if v2.votesMap.isEmpty then res == v1
    else
      res.participants == v1.participants &&
      (res.votesMap.keys == v2.votesMap.toList.head._1 :: v1.votesMap
        .filter(_._1 != v2.votesMap.toList.head._1)
        .keys)
  )

}
