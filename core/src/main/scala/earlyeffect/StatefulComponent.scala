package earlyeffect

import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js

abstract class StatefulComponent[Props, State] extends EarlyComponent[Props, State] { self =>
  def initialState(props: Props): State

  def shouldUpdate(nextProps: Props, nextState: State, previous: I): Boolean =
    previous.props != nextProps || previous.props != nextState

  def render(props: Props, state: State, instance: I): VNode

  override def instanceConstructor: js.Dynamic =
    constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[StatefulComponent.Instance[Props, State]])

}

object StatefulComponent {

  class Instance[Props, State] extends InstanceFacade[Props, StatefulComponent[Props, State], State] {

    override def render(p: js.Dynamic, s: js.Dynamic): VNodeJS = {
      val comp = lookupComponent(p)
      val ss   = lookupState(s)
      comp.render(lookupProps(p), ss, this.instance).vn
    }

    override def componentWillMount(): Unit = {
      instance.setState(lookupComponent().initialState(instance.props))
      super.componentWillMount()
    }

    override def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      lookupComponent().didUpdate(lookupProps(oldProps), lookupState(oldState), this.instance)

    override def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      lookupComponent().shouldUpdate(lookupProps(nextProps), lookupState(nextState), this.instance)

  }

  implicit def applySelf[Comp <: StatefulComponent[Comp, _], T <: Arg](self: Comp): T =
    self.apply(self).asInstanceOf[T]
}
