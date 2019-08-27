package earlyeffect

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalatest.{Assertion, Matchers}

trait EarlyOps extends Matchers {
  def render(vn: VNode): Unit = preact.render(vn, parent)

  val parent: Element = {
    val res = dom.document.createElement("div")
    dom.document.documentElement.appendChild(res)
    res
  }

  def check(s: String): Assertion = parent.innerHTML should be(s)
}
