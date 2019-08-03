package earlyeffect

import diode.{Circuit, FastEq, ModelR}
import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js

trait DiodeComponent[Props, M <: AnyRef, State] extends ComponentOps[Props, State] { self =>

  def circuit: Circuit[M]
  def reader(p: Props): ModelR[M, State]

  def zoom(get: M => State)(implicit f: FastEq[_ >: State]): ModelR[M, State] = circuit.zoom(get)

  def render(props: Props, state: State): VNode

  // we might want to do a deep equality check?
  def shouldUpdate(nextProps: Props, nextState: State, previous: InstanceOps[Props, State]): Boolean =
    previous.props != nextProps || nextState != previous.state

  override def instanceConstructor: js.Dynamic =
    constructors.getOrElseUpdate(
      defaultKey,
      js.constructorOf[DiodeComponent.Instance[Props, DiodeComponent[Props, M, State], Props]]
    )

}

object DiodeComponent {

  class Instance[Props, M <: AnyRef, State] extends BaseInstance[Props, DiodeComponent[Props, M, State], State] {

    type CM     = Circuit[M]
    type Reader = Props => ModelR[M, State]

    private var unsubscribe: () => Unit = () => ()

    def circuit: CM = lookupComponent().circuit

    def reader: Reader = lookupComponent().reader

    override def render(p: js.Dynamic, s: js.Dynamic): VNodeJS =
      lookupComponent(p).render(lookupProps(p), lookupState(s)).vn

    override def componentWillMount(): Unit = {
      lookupComponent().willMount(this.instance)
      setComponentState(reader(lookupProps()).value)
      unsubscribe = circuit.subscribe(reader(lookupProps()))(r => { setComponentState(r.value) })
    }

    override def componentWillUnmount(): Unit = unsubscribe()

    def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      lookupComponent().didUpdate(lookupProps(oldProps), lookupState(oldState), this.instance)

    def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      lookupComponent().shouldUpdate(lookupProps(nextProps), lookupState(nextState), this.instance)

  }

}
