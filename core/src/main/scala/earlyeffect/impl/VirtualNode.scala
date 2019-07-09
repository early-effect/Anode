package earlyeffect.impl

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.|

abstract class VirtualNode extends js.Object {

  @JSName("type")
  def `type`: String | js.Dynamic

  def props: js.Dynamic

  def children: js.Array[VirtualNode] = props.children.asInstanceOf[js.Array[VirtualNode]]

  def key: js.UndefOr[String]

}

object VirtualNode {

  implicit class When(vn: VirtualNode) extends Predicated(vn)
}
