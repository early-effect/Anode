package earlyeffect.impl

import diode.{Circuit, ModelR}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

class DiodeComponentInstance[Props, M <: AnyRef, State] extends ComponentJS {

  type CM     = Circuit[M]
  type Reader = Props => ModelR[M, State]

  private var unsubscribe: () => Unit = () => ()

  def circuit: CM = component().circuit

  def reader: Reader = component().reader

  def component(props: js.Dynamic = rawProps): earlyeffect.DiodeComponent[Props, M, State] =
    props.cc.asInstanceOf[earlyeffect.DiodeComponent[Props, M, State]]

  def lookup(props: js.Dynamic = rawProps): Props = props.p1.asInstanceOf[Props]

  @JSName("richProps")
  def props = lookup()

  def putState(state: State): Unit = rawSetState(js.Dynamic.literal(s = state.asInstanceOf[js.Any]))

  def fetchState(raw: js.Dynamic = this.rawState): State = raw.s.asInstanceOf[State]

  override def render(props: js.Dynamic, state: js.Dynamic): VNodeJS =
    component(props).render(lookup(props), fetchState(state)).vn

  def componentDidMount(): Unit =
    component().didMount(this)

  def componentWillMount(): Unit = {
    component().willMount(this)
    this.putState(reader(props).value)
    unsubscribe = circuit.subscribe(reader(props))(r => { this.putState(r.value) })
  }

  def componentWillUnmount() = unsubscribe()

  def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
    component().didUpdate(lookup(oldProps), fetchState(oldState), this)

  def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
    component().shouldUpdate(lookup(nextProps), fetchState(nextState), this)

}
