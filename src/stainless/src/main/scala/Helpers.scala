package prdt
import stainless.collection.List
import stainless.collection.Nil
import stainless.collection.Cons
import stainless.collection.ListMap as Map
import stainless.lang.Option
import stainless.lang.None
import stainless.lang.Some

object Helpers {
  def sum(l: List[BigInt]): BigInt = {
    l match
      case Nil()       => 0
      case Cons(x, xs) => x + sum(xs)
  }

  def combineCounts[A](
      m1: Map[A, BigInt],
      m2: Map[A, BigInt]
  ): Map[A, BigInt] = {
    m1.toList match
      case Cons((candidate, count), xs) =>
        m2.get(candidate) match
          case Some(v) => m2 + (candidate, v + count)
          case None()  => m2 + (candidate, count)
      case Nil() => m2
  }
}
