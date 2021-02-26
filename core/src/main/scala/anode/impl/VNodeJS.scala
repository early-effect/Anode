package anode.impl

import anode.VNode
import anode.impl.Preact.ComponentChildren
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.|

abstract class VNodeJS extends js.Object {

  @JSName("type")
  def `type`: String | js.Dynamic

  def props: js.Dynamic

  def key: js.UndefOr[String]

  def ref: js.UndefOr[js.Function1[dom.Element, Unit]]

}

object VNodeJS {

  implicit class VN(val vnode: VNodeJS) extends VNode {
    def rawChildren: ComponentChildren = vnode.props.children.asInstanceOf[ComponentChildren]

    def childArray: js.Array[VNodeJS] = Preact.toChildArray(rawChildren)

  }

}
