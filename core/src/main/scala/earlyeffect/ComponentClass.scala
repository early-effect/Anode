package earlyeffect

import earlyeffect.impl.Preact.AttributeOrChild

import scala.language.implicitConversions

trait ComponentClass[T] extends Component[T] { self: T =>
  private val _t: T = self
}

object ComponentClass {
  implicit def toChild[T](c: ComponentClass[T]): AttributeOrChild = c(c._t)
}
