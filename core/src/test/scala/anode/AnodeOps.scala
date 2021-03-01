package anode

import org.scalajs.dom
import org.scalajs.dom.Element

import scala.scalajs.js

trait AnodeOps {
  def render(vn: VNode): Unit = preact.render(vn, parent)

  val parent: Element = {
    val res = dom.document.createElement("div")
    dom.document.documentElement.appendChild(res)
    res
  }

  def check(s: String)              = munit.Assertions.assertEquals(parent.innerHTML, s)
  def checkAfter(n: Int)(s: String) = js.timers.setTimeout(n)(check(s))
}
