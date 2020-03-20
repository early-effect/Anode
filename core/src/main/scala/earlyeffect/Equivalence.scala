package earlyeffect

trait Equivalence[T] {
  def equivalent(a: T, b: T): Boolean
  def notEquivalent(a: T, b: T): Boolean = !equivalent(a, b)
}

object Equivalence extends EquivalenceLowPriority

trait EquivalentByValue
trait EquivalentByReference

trait EquivalenceLowPriority {

  implicit object AnyEquivalence extends Equivalence[Any] {
    override def equivalent(a: Any, b: Any): Boolean =
      a match {
        case a: AnyRef =>
          b match {
            case b: AnyRef with EquivalentByValue =>
              a == b
            case b: AnyRef =>
              a eq b
            case b =>
              a == b
          }
        case a => a == b
      }
  }

}
