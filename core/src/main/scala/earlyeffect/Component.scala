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
  def shouldUpdate(nextProps: Props, previous: I): Boolean = previous.props != nextProps

  val constructor: js.Dynamic = Component.constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[I])

  def apply(props: Props): VNode =
    EarlyEffect.h(
      constructor,
      js.Dictionary[js.Any](("p1", props.asInstanceOf[js.Any]), ("cc", self.asInstanceOf[js.Any]))
    )
}

object Component {
  val constructors = js.Dictionary[js.Dynamic]()
}
