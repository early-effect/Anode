package earlyeffect

import earlyeffect.impl.{ComponentInstance, EarlyEffect}

import scala.language.implicitConversions
import scala.scalajs.js

abstract class Component[Props] { self =>

  type I = ComponentInstance[Props]

  def render(props: Props): VNode

  def didMount(instance: I): Unit                   = ()
  def willMount(instance: I): Unit                  = ()
  def didUpdate(oldProps: Props, instance: I): Unit = ()

  // we might want to do a deep equality check?
  def shouldUpdate(nextProps: Props, instance: I): Boolean =
    instance.lookup() != nextProps

  def apply(props: Props): VNode = {
    val tag = js.constructorTag[ComponentInstance[Props]]
    EarlyEffect.h(
      tag.constructor,
      js.Dictionary[js.Any](("p1", props.asInstanceOf[js.Any]), ("cc", self.asInstanceOf[js.Any]))
    )
  }
}

object Component {
  implicit def cToArg[C <: Component[C]](c: C): Arg = c(c)
}
