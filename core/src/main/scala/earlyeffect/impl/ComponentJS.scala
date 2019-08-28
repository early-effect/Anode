package earlyeffect.impl

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportStatic, JSImport, JSName}

@JSImport("preact", "Component")
@js.native
abstract class ComponentJS extends js.Object {

  @JSName("props")
  @inline
  final def rawProps: js.Dynamic = js.native

  @JSName("state")
  @inline
  final def rawState: js.Dynamic = js.native

  @JSName("setState")
  final def rawSetState(newState: js.Dynamic): Unit = js.native

  @JSName("base")
  @inline
  final def rawBase: dom.Element = js.native

  def render(props: js.Dynamic, state: js.Dynamic): VNodeJS

  def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit

  def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, nextContext: js.Dynamic): Boolean

  def componentWillReceiveProps(nextProps: js.Dynamic, nextContext: js.Dynamic): Unit

  def componentDidMount(): Unit

  def componentWillMount(): Unit

  def componentWillUnmount(): Unit

}
