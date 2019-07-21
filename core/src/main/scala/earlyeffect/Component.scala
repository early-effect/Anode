package earlyeffect

import earlyeffect.impl.{ComponentInstance, EarlyEffect}

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.UndefOr

abstract class Component[Props] { self =>

  type I = ComponentInstance[Props]

  def render(props: Props): VNode

  def didMount(instance: I): Unit                   = ()
  def willMount(instance: I): Unit                  = ()
  def didUpdate(oldProps: Props, instance: I): Unit = ()

  // we might want to do a deep equality check?
  def shouldUpdate(nextProps: Props, previous: I): Boolean =
    previous.props != nextProps

  val constructor: js.Dynamic =
    Component.constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[I])

  def apply(props: Props): VNode =
    EarlyEffect.h(
      constructor,
      js.Dictionary[js.Any](
        ("p1", props.asInstanceOf[js.Any]),
        ("cc", self.asInstanceOf[js.Any])
      )
    )
}

abstract class Container[Props] extends Component[Props] { self =>
  def render(props: Props): VNode = render(props, Seq.empty: _*)

  def render(props: Props, children: Child*): VNode

  def apply(props: Props)(children: Child*): VNode =
    EarlyEffect.h(
      constructor,
      js.Dictionary[js.Any](
        ("p1", props.asInstanceOf[js.Any]),
        ("cc", self.asInstanceOf[js.Any])
      ),
      children: _*
    )
}

object Component {
  val constructors = js.Dictionary[js.Dynamic]()

  implicit def applySelf[Comp <: Component[Comp], T <: Arg](self: Comp): T = self.apply(self).asInstanceOf[T]
}
