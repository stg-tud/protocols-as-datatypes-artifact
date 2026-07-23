package prdt
import stainless.annotation.*
import stainless.collection.Cons
import stainless.collection.List
import stainless.collection.ListMap as Map
import stainless.collection.ListMapLemmas.*
import stainless.collection.ListOps
import stainless.collection.Nil
import stainless.lang.BooleanDecorations
import stainless.lang.None
import stainless.lang.Option
import stainless.lang.Set
import stainless.lang.Some
import stainless.lang.StaticChecks.*
import stainless.lang.decreases
import stainless.proof.*
import stainless.proof.check

type Id = LocalUid

trait Consensus[A] {

  type State = A
  type Action
  type Delta

  def merge(s1: State, s2: Delta): State

  def apply(s: State, a: Action, i: Id): Delta

  def precond(s: State, a: Action, i: Id): Boolean

  // returns true if s1 <= s2 w.r.t. the decision order
  def decisionOrder(s1: State, s2: State): Boolean

  @pure @ghost @law
  def stability(
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
      (precond(s1, e1, n1))
  }

  @pure @ghost @law
  def monotonicity(
      n: Id,
      e: Action,
      s: State
  ): Boolean = {
    val delta = apply(s, e, n)
    val s1 = merge(s, delta)
    precond(s, e, n) ==> decisionOrder(s, s1)
  }
}