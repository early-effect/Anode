package anode

import org.scalajs.dom
import org.scalajs.dom.Element

import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle

trait AnodeOps {
  def render(vn: VNode): Unit = preact.render(vn, parent)
  def render(args: Args): Unit = preact.render(E.div(args), parent)

  val parent: Element = {
    val res = dom.document.createElement("div")
    dom.document.documentElement.appendChild(res)
    res
  }

  def check(s: String): Unit = munit.Assertions.assertEquals(parent.innerHTML, s)
  def checkAfter(n: Int)(s: String): SetTimeoutHandle = js.timers.setTimeout(n)(check(s))
}
