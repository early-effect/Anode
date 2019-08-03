package earlyeffect

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

abstract class BaseInstance[Props, +Component <: ComponentOps[Props, State], State] extends impl.ComponentJS { self =>
  import BaseInstance._

  def componentDidMount(): Unit =
    component().didMount(self)

  def componentWillMount(): Unit =
    component().willMount(self)

  def componentWillUnmount(): Unit = component().willUnMount(self)

  @JSName("earlyEffectProps")
  def props(p: js.Dynamic = rawProps): Props = p.props

  @JSName("earlyEffectComponent")
  def component(p: js.Dynamic = rawProps): Component = p.component

  @JSName("earlyEffectState")
  def state(s: js.Dynamic = rawState): State = s.state

  @JSName("earlyEffectSetState")
  def setState(state: State): Unit =
    rawSetState(js.Dictionary(dictionaryNames.State -> state.asInstanceOf[js.Any]).asInstanceOf[js.Dynamic])

}

object BaseInstance {
  implicit class EarlyProps(d: js.Dynamic) {
    import dictionaryNames._
    def cast[T](field: String): T = d.selectDynamic(field).asInstanceOf[T]
    def props[T]: T               = cast(Props)
    def state[T]: T               = cast(State)
    def component[T]: T           = cast(ComponentConstructor)
  }
}
