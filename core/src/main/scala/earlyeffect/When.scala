package earlyeffect

class When(p: => Boolean) {
  def apply[A, B <: A](a: => A, b: B): A = if (p) a else b
  def apply(c: => Child): Child          = apply(c, EmptyChild)
  def apply(a: => Arg): Arg              = apply(a, Empty)
}

object When {
  def apply(p: => Boolean) = new When(p)
}
