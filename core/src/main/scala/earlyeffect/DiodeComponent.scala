package earlyeffect

import diode.{Circuit, FastEq, ModelR}
import earlyeffect.impl.{ComponentJS, EarlyEffect, VNodeJS}

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

trait DiodeComponent[Props, M <: AnyRef, State] { self =>

  def circuit: Circuit[M]
  def reader(p: Props): ModelR[M, State]

  def zoom(get: M => State)(implicit f: FastEq[_ >: State]): ModelR[M, State] = circuit.zoom(get)

  type I = DiodeComponent.Instance[Props, M, State]
  def render(props: Props, state: State): VNode

  def didMount(instance: I): Unit                                    = ()
  def willMount(instance: I): Unit                                   = ()
  def didUpdate(oldProps: Props, oldState: State, instance: I): Unit = ()

  // we might want to do a deep equality check?
  def shouldUpdate(nextProps: Props, nextState: State, previous: I): Boolean =
    previous.props != nextProps || nextState != previous.fetchState()

  val constructor: js.Dynamic =
    constructors.getOrElseUpdate(defaultKey, js.constructorOf[I])

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

object DiodeComponent {

  class Instance[Props, M <: AnyRef, State] extends ComponentJS {

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

    def componentWillUnmount(): Unit = unsubscribe()

    def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      component().didUpdate(lookup(oldProps), fetchState(oldState), this)

    def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      component().shouldUpdate(lookup(nextProps), fetchState(nextState), this)

  }

}
