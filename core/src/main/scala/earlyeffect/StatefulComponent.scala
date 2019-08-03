package earlyeffect

import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js

abstract class StatefulComponent[Props, State] extends ComponentOps[Props, State] { self =>

  def shouldUpdate(nextProps: Props, nextState: State, previous: I): Boolean =
    previous.props() != nextProps || previous.state() != nextState

  override def willMount(instance: I): Unit =
    super.willMount(instance)

  def render(props: Props, state: State, instance: I): VNode

  override def instanceConstructor: js.Dynamic =
    constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[StatefulComponent.Instance[Props, State]])

}

object StatefulComponent {

  class Instance[Props, State] extends BaseInstance[Props, StatefulComponent[Props, State], State] {

    override def render(p: js.Dynamic, s: js.Dynamic): VNodeJS = {
      val comp = component(p)
      val ss   = state(s)
      comp.render(props(p), ss, this).vn
    }

    def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      component().didUpdate(props(oldProps), state(oldState), this)

    def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      component().shouldUpdate(props(nextProps), state(nextState), this)

  }

  implicit def applySelf[Comp <: StatefulComponent[Comp, _], T <: Arg](self: Comp): T =
    self.apply(self).asInstanceOf[T]
}
