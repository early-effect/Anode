package earlyeffect.impl

import earlyeffect._
import earlyeffect.impl.Preact.{AnyDictionary, FunctionalComponent, NoArgFunctionalComponent}
import org.scalajs.dom

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

abstract class AorC

@js.native
@JSImport("preact", JSImport.Default)
object Preact extends js.Object {

  val Fragment: js.Dynamic = js.native

  type AnyDictionary = js.Dictionary[js.Any]

  type AttributeOrChildJS = VNodeJS | Attribute | Declaration | String | Double | Args | js.Object

  type ComponentChild    = VNodeJS | js.Object | String | Double | Boolean
  type ComponentChildren = js.Array[ComponentChild] | ComponentChild

  type ChildJS = VNodeJS | String

  type NoArgFunctionalComponent = js.Function0[VNode]

  type FunctionalComponent = js.Function1[js.Dynamic, VNode]

  def toChildArray(children: ComponentChildren): js.Array[VNodeJS] = js.native

  def h(`type`: js.Dynamic, params: AnyDictionary, children: ComponentChildren): VNodeJS =
    js.native

  def h(`type`: NoArgFunctionalComponent, params: AnyDictionary, children: ComponentChildren): VNodeJS =
    js.native

  def h(`type`: FunctionalComponent, params: AnyDictionary, children: ComponentChildren): VNodeJS = js.native

  def h(`type`: String, params: AnyDictionary, children: ComponentChildren): VNodeJS =
    js.native

  def h(`type`: js.Dynamic, params: AnyDictionary): VNodeJS = js.native

  def h(`type`: NoArgFunctionalComponent, params: AnyDictionary): VNodeJS = js.native

  def h(`type`: FunctionalComponent, params: AnyDictionary): VNodeJS = js.native

  def h(`type`: String, params: AnyDictionary): VNodeJS = js.native

  def render(node: VNodeJS, parent: dom.Element): Unit = js.native

  def render(node: VNodeJS, parent: dom.Element, replaceNode: dom.Element): Unit = js.native

  def rerender(): Unit = js.native

}

object EarlyEffect {

  implicit def nativeToVNode(v: VNodeJS): VNode = VNode(v)

  def h(`type`: js.Dynamic, params: AnyDictionary, children: js.Array[Child]): VNode =
    Preact.h(`type`, params, children.map(_.value))

  def h(`type`: NoArgFunctionalComponent, params: AnyDictionary, children: js.Array[Child]): VNode =
    Preact.h(`type`, params, children.map(_.value))

  def h(`type`: FunctionalComponent, params: AnyDictionary, children: js.Array[Child]): VNode =
    Preact.h(`type`, params, children.map(_.value))

  def h(`type`: String, params: AnyDictionary, children: js.Array[Child]): VNode =
    Preact.h(`type`, params, children.map(_.value))

  def h(`type`: js.Dynamic, params: AnyDictionary): VNodeJS = Preact.h(`type`, params)

  def h(`type`: NoArgFunctionalComponent, params: AnyDictionary): VNodeJS = Preact.h(`type`, params)

  def h(`type`: FunctionalComponent, params: AnyDictionary): VNodeJS = Preact.h(`type`, params)

  def h(`type`: String, params: AnyDictionary): VNodeJS = Preact.h(`type`, params)

}
