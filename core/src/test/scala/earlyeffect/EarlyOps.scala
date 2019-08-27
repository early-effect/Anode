package earlyeffect

import org.scalajs.dom
import org.scalajs.dom.Element

trait EarlyOps {
  def render(vn: VNode): Unit = preact.render(vn, div)

  val div: Element = {
    val res = dom.document.createElement("div")
    dom.document.documentElement.appendChild(res)
    res
  }
}
