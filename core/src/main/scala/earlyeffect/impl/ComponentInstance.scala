package earlyeffect.impl

import scala.scalajs.js
import earlyeffect._

class ComponentInstance[Props] extends Component {

  def component(props: js.Dynamic = rawProps): earlyeffect.Component[Props] =
    props.cc.asInstanceOf[earlyeffect.Component[Props]]

  def lookup(props: js.Dynamic = rawProps): Props = props.p1.asInstanceOf[Props]

  override def render(props: js.Dynamic, state: js.Dynamic): VNodeJS =
    component(props).render(lookup(props)).vn

  def componentDidMount(): Unit =
    component().didMount(this)

  def componentWillMount(): Unit = component().willMount(this)

  def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
    component().didUpdate(lookup(oldProps), this)

  def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
    component().shouldUpdate(lookup(nextProps), this)

}
