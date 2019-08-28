package earlyeffect

import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.UndefOr

abstract class StatefulComponent[Props, State] extends EarlyComponent[Props, State] { self =>

  def initialState(props: Props): State

  def deriveState(props: Props, oldState: State) = oldState

  def shouldUpdate(nextProps: Props, nextState: State, previous: Instance): Boolean =
    previous.props != nextProps || previous.props != nextState

  def render(props: Props, state: State, instance: Instance): VNode

  override def instanceConstructor: js.Dynamic =
    constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[StatefulComponent.Instance[Props, State]])

}

object StatefulComponent {

  class Instance[Props, State] extends InstanceFacade[Props, StatefulComponent[Props, State], State] { self =>

    override def componentWillReceiveProps(nextProps: js.Dynamic, nextContext: js.Dynamic): Unit = {
      val c   = lookupComponent(nextProps)
      val res = c.deriveState(lookupProps(nextProps), lookupState())
      setState(res)
    }

    override def render(p: js.Dynamic, s: js.Dynamic): VNodeJS = {
      val comp  = lookupComponent(p)
      val state = lookupState(s)
      comp.render(props, state, self).vnode
    }

    override def componentWillMount(): Unit = {
      instance.setState(lookupComponent().initialState(instance.props))
      super.componentWillMount()
    }

    override def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      lookupComponent().didUpdate(
        lookupProps(oldProps),
        lookupState(oldState),
        this.instance,
        snapshot.asInstanceOf[UndefOr[Instance[Props, State]]].map(_.instance)
      )

    override def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      lookupComponent().shouldUpdate(lookupProps(nextProps), lookupState(nextState), this.instance)

  }

  implicit def applySelf[Comp <: StatefulComponent[Comp, _], T <: Arg](self: Comp): T =
    self.apply(self).asInstanceOf[T]
}
