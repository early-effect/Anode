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
      anode.preact.render(App2, document.documentElement, e)
    }
  }
}

object App2 extends Component[Unit] {
  import anode.Formable._
  import defaultImplicits._
  case class Wife(name:String)
  case class Foo(firsName:String, lastName:String, wife: Wife)
  override def render(props: Unit): VNode = {
    E.body(Foo("Russ","White",Wife("Michelle")).form(x => log("res", x.toString)))
  }
}
