package anode

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.scalajs.js

trait AnodeOps extends Matchers {
  def render(vn: VNode): Unit = preact.render(vn, parent)

  val parent: Element = {
    val res = dom.document.createElement("div")
    dom.document.documentElement.appendChild(res)
    res
  }

  def check(s: String): Assertion   = parent.innerHTML should be(s)
  def checkAfter(n: Int)(s: String) = js.timers.setTimeout(n)(parent.innerHTML should be(s))
}
