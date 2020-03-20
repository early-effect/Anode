package earlyeffect

import diode._
import earlyeffect.impl.VNodeJS

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

trait CircuitComponent[Props, Model <: AnyRef, State] extends EarlyComponent[Props, State] with Equivalence {
  theComponent =>
  def circuit: Circuit[Model]
  def render(props: Props, state: State): VNode
  def modelReader(p: Props): ModelR[Model, State]
  def zoom(get: Model => State)(implicit f: FastEq[_ >: State]): ModelR[Model, State] = circuit.zoom(get)

  def shouldUpdate(nextProps: Props, nextState: State, previous: Instance): Boolean =
    notEquivalent(previous.state, nextState) || notEquivalent(previous.props, nextProps)
  override lazy val instanceConstructor: js.Dynamic = js.constructorOf[CircuitInstance]
  final private class CircuitInstance extends InstanceFacade[Props, State] {
    type Reader = Props => ModelR[Model, State]
    private var unsubscribe: () => Unit = () => ()
    override def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      didUpdate(
        lookupProps(oldProps),
        lookupState(oldState),
        instance = this,
        snapshot.asInstanceOf[js.UndefOr[CircuitComponent[Props, Model, State]#Instance]]
      )
    override def componentDidMount(): Unit = didMount(this)

    @JSName("render")
    override def renderJS(p: js.Dynamic, s: js.Dynamic): VNodeJS =
      addSelectors(render(lookupProps(p), lookupState(s)), this)
    override def componentWillMount(): Unit = {
      willMount(this)
      setState(modelReader(props).value)
      unsubscribe = circuit.subscribe(modelReader(props))(x => setState(x.value))
    }
    override def componentWillUnmount(): Unit = unsubscribe()
    override def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      theComponent.shouldUpdate(lookupProps(nextProps), lookupState(nextState), this)
  }
}
