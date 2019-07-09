package earlyeffect.impl

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

@JSImport("preact", "Component")
@js.native
abstract class Component extends js.Object {

  @JSName("props")
  @inline
  final def rawProps: js.Dynamic = js.native

  @JSName("state")
  @inline
  final var rawState: js.Dynamic = js.native

  @JSName("setState")
  final def rawSetState(newState: js.Dynamic): Unit = js.native

  @JSName("base")
  @inline
  final def rawBase: dom.Element = js.native

  def render(props: js.Dynamic, state: js.Dynamic): VirtualNode

}
