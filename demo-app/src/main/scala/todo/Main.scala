package todo

import anode._
import org.scalajs.dom.document

import scala.scalajs.js.annotation.JSExportTopLevel

object Main {

  @JSExportTopLevel("main")
  def main(): Unit = {
    if (document.readyState == "complete" || document.readyState == "interactive") {
      start
    } else {
      document.addEventListener(
        "DOMContentLoaded",
        (_: Any) => start,
        false,
      )
    }
    def start: Unit = {
      val e = document.createElement("body")
      document.documentElement.replaceChild(e, document.body)
      anode.preact.render(App, document.documentElement, e)
    }
  }
}

