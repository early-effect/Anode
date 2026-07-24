package todo

import anode._
import org.scalajs.dom.document
import org.scalajs.dom.DocumentReadyState

object Main {

  def main(args: Array[String]): Unit = {
    if (
      document.readyState == DocumentReadyState.complete ||
      document.readyState == DocumentReadyState.interactive
    ) {
      start()
    } else {
      document.addEventListener(
        "DOMContentLoaded",
        (_: Any) => start(),
        false,
      )
    }
    def start(): Unit = {
      val e = document.createElement("body")
      document.documentElement.replaceChild(e, document.body)
      anode.preact.render(App, document.documentElement, e)
    }
  }
}
