package earlyeffect.impl

abstract class Predicated[A](a: A) {
  def when(predicate: => Boolean): A = if (predicate) a else null.asInstanceOf[A]
}
