package earlyeffect.impl

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.|

abstract class VNodeJS extends js.Object {

  @JSName("type")
  def `type`: String | js.Dynamic

  def props: js.Dynamic

  def children: js.Array[VNodeJS] = props.children.asInstanceOf[js.Array[VNodeJS]]

  def key: js.UndefOr[String]

}

object VNodeJS {
  implicit class When(vn: VNodeJS) extends Predicated(vn)
}
