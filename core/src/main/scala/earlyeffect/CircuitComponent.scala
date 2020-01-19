package earlyeffect

import diode._
import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

trait CircuitComponent[Props, M <: AnyRef, State] extends EarlyComponent[Props, State] {
  theComponent =>

  def circuit: Circuit[M]

  def render(props: Props, state: State): VNode

  def modelReader(p: Props): ModelR[M, State]

  def zoom(get: M => State)(implicit f: FastEq[_ >: State]): ModelR[M, State] = circuit.zoom(get)

  def shouldUpdate(nextProps: Props, nextState: State, previous: I): Boolean =
    nextState != previous.state || nextProps != previous.props

  override lazy val instanceConstructor: js.Dynamic = js.constructorOf[Instance]

  class Instance extends InstanceFacade[Props, State] {

    type Reader = Props => ModelR[M, State]

    private var unsubscribe: () => Unit = () => ()

    override def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      didUpdate(
        lookupProps(oldProps),
        lookupState(oldState),
        instance = this,
        snapshot.asInstanceOf[js.UndefOr[CircuitComponent[Props, M, State]#I]]
      )

    override def componentDidMount(): Unit = didMount(this)

    @JSName("render")
    override def renderJS(p: js.Dynamic, s: js.Dynamic): VNodeJS =
      addSelectors(render(lookupProps(p), lookupState(s)), theComponent)

    override def componentWillMount(): Unit = {
      theComponent.willMount(this)
      setState(modelReader(props).value)
      unsubscribe = circuit.subscribe(modelReader(props))(x => setState(x.value))
    }

    override def componentWillUnmount(): Unit = unsubscribe()

    override def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      theComponent.shouldUpdate(lookupProps(nextProps), lookupState(nextState), this)

  }

}
