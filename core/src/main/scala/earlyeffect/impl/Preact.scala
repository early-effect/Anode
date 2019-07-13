package earlyeffect.impl

import earlyeffect.{Attribute, NodeArgs}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

abstract class AorC

@js.native
@JSImport("preact", JSImport.Default)
object Preact extends js.Object {

  val Fragment: js.Dynamic = js.native

  type AnyDictionary = js.Dictionary[js.Any]

  type AttributeOrChild = VirtualNode | Attribute | String | Int | Double | NodeArgs | js.Object

  type Child = VirtualNode | String

  type StatelessFunctionalComponent = js.Function0[VirtualNode]

  type FunctionalComponent = js.Function1[js.Dynamic, VirtualNode]

  def h(`type`: js.Dynamic, params: AnyDictionary, children: Child*): VirtualNode =
    js.native

  def h(`type`: StatelessFunctionalComponent, params: AnyDictionary, children: Child*): VirtualNode =
    js.native

  def h(`type`: FunctionalComponent, params: AnyDictionary, children: Child*): VirtualNode = js.native

  def h(`type`: String, params: AnyDictionary, children: Child*): VirtualNode =
    js.native

  def h(`type`: js.Dynamic, params: AnyDictionary): VirtualNode = js.native

  def h(`type`: StatelessFunctionalComponent, params: AnyDictionary): VirtualNode = js.native

  def h(`type`: FunctionalComponent, params: AnyDictionary): VirtualNode = js.native

  def h(`type`: String, params: AnyDictionary): VirtualNode = js.native

  def render(node: VirtualNode, parent: dom.Element): Unit = js.native

  def render(node: VirtualNode, parent: dom.Element, replaceNode: dom.Element): Unit = js.native

  def rerender(): Unit = js.native

}
