package earlyeffect

import earlyeffect.impl.{ComponentJS, EarlyEffect, VNodeJS}

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

abstract class StatefulComponent[Props, State] { self =>
  type I = StatefulComponent.Instance[Props, State]

  def didMount(instance: I): Unit                   = ()
  def willMount(instance: I): Unit                  = ()
  def didUpdate(oldProps: Props, instance: I): Unit = ()

  def shouldUpdate(nextProps: Props, nextState: State, previous: I): Boolean =
    previous.props != nextProps || previous.state != nextState

  def render(props: Props, state: State, instance: I): VNode

  val constructor: js.Dynamic =
    constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[I])

  val defaultKey = self.getClass.getName

  def apply(props: Props): VNode =
    EarlyEffect.h(
      constructor,
      js.Dictionary[js.Any](
        ("key", defaultKey),
        ("p1", props.asInstanceOf[js.Any]),
        ("cc", self.asInstanceOf[js.Any])
      )
    )

}

object StatefulComponent {

  class Instance[Props, State] extends ComponentJS {

    def component(props: js.Dynamic = rawProps): earlyeffect.StatefulComponent[Props, State] =
      props.cc.asInstanceOf[earlyeffect.StatefulComponent[Props, State]]

    def lookup(props: js.Dynamic = rawProps): Props = props.p1.asInstanceOf[Props]

    @JSName("richSetState")
    def setState(state: State): Unit = rawSetState(js.Dynamic.literal(s = state.asInstanceOf[js.Any]))

    @JSName("richState")
    def state: State = this.rawState.s.asInstanceOf[State]

    @JSName("richProps")
    def props = lookup()

    override def render(props: js.Dynamic, state: js.Dynamic): VNodeJS = {
      val comp = component(props)
      val p    = lookup(props)
      comp.render(p, state.s.asInstanceOf[State], this).vn
    }

    def componentDidMount(): Unit =
      component().didMount(this)

    def componentWillMount(): Unit =
      component().willMount(this)

    def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      component().didUpdate(lookup(oldProps), this)

    def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      component().shouldUpdate(lookup(nextProps), nextState.s.asInstanceOf[State], this)

  }

  implicit def applySelf[Comp <: StatefulComponent[Comp, _], T <: Arg](self: Comp): T = self.apply(self).asInstanceOf[T]
}
