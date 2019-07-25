package earlyeffect

import earlyeffect.impl.{EarlyEffect, StatefulComponentInstance}

import scala.language.implicitConversions
import scala.scalajs.js

abstract class StatefulComponent[Props, State] { self =>
  type I = StatefulComponentInstance[Props, State]

  def didMount(instance: I): Unit                   = ()
  def willMount(instance: I): Unit                  = ()
  def didUpdate(oldProps: Props, instance: I): Unit = ()

  def shouldUpdate(nextProps: Props, nextState: State, previous: I): Boolean =
    previous.props != nextProps || previous.state != nextState

  def render(props: Props, state: State, instance: I): VNode

  val constructor: js.Dynamic =
    StatefulComponent.constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[I])

  def apply(props: Props): VNode =
    EarlyEffect.h(
      constructor,
      js.Dictionary[js.Any](
        ("p1", props.asInstanceOf[js.Any]),
        ("cc", self.asInstanceOf[js.Any])
      )
    )

}

object StatefulComponent {
  val constructors = js.Dictionary[js.Dynamic]()

  implicit def applySelf[Comp <: StatefulComponent[Comp, _], T <: Arg](self: Comp): T = self.apply(self).asInstanceOf[T]
}
