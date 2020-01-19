package earlyeffect

import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSName

trait StatefulComponent[Props, State] extends EarlyComponent[Props, State] { theComponent =>

  def initialState(props: Props): State

  def deriveState(props: Props, oldState: State) = oldState

  def shouldUpdate(nextProps: Props, nextState: State, previous: I): Boolean =
    previous.props != nextProps || previous.props != nextState

  def render(props: Props, state: State, instance: I): VNode

  override lazy val instanceConstructor: js.Dynamic = js.constructorOf[Instance]

  final private class Instance extends InstanceFacade[Props, State] {

    override def componentDidMount(): Unit = didMount(this)

    override def componentWillUnmount(): Unit = willMount(this)

    @JSName("render")
    override def renderJS(props: js.Dynamic, state: js.Dynamic): VNodeJS =
      render(lookupProps(props), lookupState(state), instance = this)

    override def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, nextContext: js.Dynamic): Boolean =
      theComponent.shouldUpdate(lookupProps(nextProps), lookupState(nextState), previous = this)

    override def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      theComponent.didUpdate(
        lookupProps(oldProps),
        lookupState(oldState),
        instance = this,
        snapshot.asInstanceOf[UndefOr[StatefulComponent[Props, State]#I]]
      )

    override def componentWillReceiveProps(nextProps: js.Dynamic, nextContext: js.Dynamic): Unit = {
      val res = theComponent.deriveState(lookupProps(nextProps), lookupState())
      setState(res)
    }

    override def componentWillMount(): Unit = {
      setState(theComponent.initialState(props))
      willMount(this)
    }
  }

}

object StatefulComponent {
  implicit def applySelf[Comp <: StatefulComponent[Comp, _], T <: Arg](self: Comp): T =
    self.apply(self).asInstanceOf[T]
}
