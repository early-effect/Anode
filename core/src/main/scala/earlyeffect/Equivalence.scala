package earlyeffect

trait Equivalence {
  trait ByValue

  def equivalent(a: Any, b: Any): Boolean =
    a match {
      case a: AnyRef =>
        b match {
          case b: AnyRef with ByValue =>
            a == b
          case b: AnyRef =>
            a eq b
          case b =>
            a == b
        }
      case a => a == b
    }

  def notEquivalent(a: Any, b: Any): Boolean = !equivalent(a, b)
}
