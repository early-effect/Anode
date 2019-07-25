package earlyeffect

class When(p: Boolean) {
  def apply(c: => Child): Child = if (p) c else EmptyChild
  def apply(a: => Arg): Arg     = if (p) a else Empty
}

object When {
  def apply(p: => Boolean) = new When(p)
}
