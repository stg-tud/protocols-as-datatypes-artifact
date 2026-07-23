package prdt
import stainless.annotation._
// opaque currently causes too many weird issues with library integrations, in particular the json libraries can no longer auto serialize
/** Uid’s are serializable abstract unique Ids. Currently implemented as
  * Strings, but subject to change.
  */
case class Uid(delegate: String)

object Uid {
  @library @extern
  given ordering: Ordering[Uid] = Ordering.String.on(_.delegate)

  def predefined(s: String): Uid = Uid(s)
  def unwrap(id: Uid): String = id.delegate
  val zero: Uid = Uid("")

  extension (s: String) def asId: Uid = Uid(s)

  // given toLocal: Conversion[Uid, LocalUid] = x => LocalUid(x)
}

/** Operations may require an ID of the replica doing a modification. We provide
  * it as it’s own opaque type, to make it obvious that this should not be just
  * any ID. Use [[Uid]] if you want to store an ID in a replicated data
  * structure.
  */
case class LocalUid(uid: Uid)
object LocalUid {
  // given ordering: Ordering[LocalUid] = Uid.ordering.on(_.uid)

  // extension (s: String) def asId: LocalUid = predefined(s)

  // def predefined(s: String): LocalUid     = Uid.predefined(s).convert
  def unwrap(id: LocalUid): Uid = id.uid
  def replicaId(using rid: LocalUid): Uid = rid.uid
}
trait Bottom[A] {
  def empty: A

  /** Tests if the state is an identity of [[Lattice.merge]], i.e., forall `a`
    * with `isEmpty(a)` we require that `a `merge` b == b`. See [[Bottom]] for
    * cases when an empty element can be generated.
    */
  extension (value: A) def isEmpty: Boolean = value == empty
}

object Bottom {

  @library
  def provide[A](v: A): Bottom[A] = new Bottom[A]:
    override val empty: A = v

  @library
  def empty[A](using bottom: Bottom[A]): A = bottom.empty

  @library
  def isEmpty[A](v: A)(using bottom: Bottom[A]): Boolean = bottom.isEmpty(v)

  @library
  def apply[A](using bottom: Bottom[A]): Bottom[A] = bottom
}

object Syntax {
  def precondition[P: Bottom](condition: Boolean)(update: => P): P = {
    if (condition) then update
    else Bottom[P].empty
  }.ensuring((res: P) =>
    (condition && (res == update)) ||
      (res == Bottom[P].empty)
  )
}

enum Decision[A]:
  case Decided(value: A)
  case Undecided()
