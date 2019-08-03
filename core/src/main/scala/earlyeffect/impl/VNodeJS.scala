package earlyeffect.impl

import earlyeffect.impl.Preact.ChildJS
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.|

abstract class VNodeJS extends js.Object {

  @JSName("type")
  def `type`: String | js.Dynamic

  def props: js.Dynamic

  def children: js.Array[ChildJS] = props.children.asInstanceOf[js.Array[ChildJS]]

  def key: js.UndefOr[String]

  def ref: js.UndefOr[js.Function1[dom.Element, Unit]]

}
