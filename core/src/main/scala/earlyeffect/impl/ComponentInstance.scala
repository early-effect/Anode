package earlyeffect.impl

import earlyeffect._

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSName

class ComponentInstance[Props] extends ComponentJS {

  def component(props: js.Dynamic = rawProps): earlyeffect.Component[Props] =
    props.cc.asInstanceOf[earlyeffect.Component[Props]]

  def lookup(props: js.Dynamic = rawProps): Props = props.p1.asInstanceOf[Props]

  @JSName("richProps")
  def props = lookup()

  override def render(props: js.Dynamic, state: js.Dynamic): VNodeJS = {
    val c: UndefOr[js.Dynamic] = props.children
    val comp                   = component(props)
    val p                      = lookup(props)
    c.fold(comp.render(p).vn) { c =>
      val l: UndefOr[Int] = c.length.asInstanceOf[Int]
      l.fold(comp.asInstanceOf[Container[Props]].render(p, VNode(c.asInstanceOf[VNodeJS])).vn)(
        _ => comp.asInstanceOf[Container[Props]].render(p, c.asInstanceOf[js.Array[VNodeJS]].map(VNode): _*).vn
      )
    }
  }

  def componentDidMount(): Unit =
    component().didMount(this)

  def componentWillMount(): Unit =
    component().willMount(this)

  def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
    component().didUpdate(lookup(oldProps), this)

  def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
    component().shouldUpdate(lookup(nextProps), this)

}
