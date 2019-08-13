package earlyeffect

import diode._
import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.UndefOr

trait CircuitComponent[Props, M <: AnyRef, State] extends EarlyComponent[Props, State] {
  self =>

  def circuit: Circuit[M]

  def render(props: Props, state: State): VNode

  def modelReader(p: Props): ModelR[M, State]

  def zoom(get: M => State)(implicit f: FastEq[_ >: State]): ModelR[M, State] = circuit.zoom(get)

  def shouldUpdate(nextProps: Props, nextState: State, previous: Instance): Boolean =
    nextState != previous.state || nextProps != previous.props

  override def instanceConstructor: js.Dynamic =
    constructors.getOrElseUpdate(
      defaultKey,
      js.constructorOf[CircuitComponent.Instance[Props, CircuitComponent[Props, M, State], Props]]
    )

}

object CircuitComponent {

  class Instance[Props, M <: AnyRef, State] extends InstanceFacade[Props, CircuitComponent[Props, M, State], State] {

    type Reader = Props => ModelR[M, State]

    private var unsubscribe: () => Unit = () => ()

    override def render(p: js.Dynamic, s: js.Dynamic): VNodeJS = {
      val comp = lookupComponent(p)
      comp.render(props, lookupState(s)).vnode
    }

    override def componentWillMount(): Unit = {
      val component = lookupComponent()
      component.willMount(instance)
      instance.setState(component.modelReader(instance.props).value)
      unsubscribe = component.circuit.subscribe(component.modelReader(instance.props))(x => instance.setState(x.value))
    }

    override def componentWillUnmount(): Unit = unsubscribe()

    def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      lookupComponent().didUpdate(
        lookupProps(oldProps),
        lookupState(oldState),
        this.instance,
        snapshot.asInstanceOf[UndefOr[Instance[Props, M, State]]].map(_.instance)
      )

    def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      lookupComponent().shouldUpdate(lookupProps(nextProps), lookupState(nextState), this.instance)

  }

}
