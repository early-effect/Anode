package earlyeffect.impl

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName
import earlyeffect._

class StatefulComponentInstance[Props, State] extends ComponentJS {

  def component(props: js.Dynamic = rawProps): earlyeffect.StatefulComponent[Props, State] =
    props.cc.asInstanceOf[earlyeffect.StatefulComponent[Props, State]]

  def lookup(props: js.Dynamic = rawProps): Props = props.p1.asInstanceOf[Props]

  @JSName("richSetState")
  def setState(state: State): Unit = rawSetState(js.Dynamic.literal(s = state.asInstanceOf[js.Any]))

  @JSName("richState")
  def state: State = this.rawState.s.asInstanceOf[State]

  @JSName("richProps")
  def props = lookup()

  override def render(props: js.Dynamic, state: js.Dynamic): VNodeJS = {
    val comp = component(props)
    val p    = lookup(props)
    comp.render(p, state.s.asInstanceOf[State], this).vn
  }

  def componentDidMount(): Unit =
    component().didMount(this)

  def componentWillMount(): Unit =
    component().willMount(this)

  def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
    component().didUpdate(lookup(oldProps), this)

  def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
    component().shouldUpdate(lookup(nextProps), nextState.s.asInstanceOf[State], this)

}
