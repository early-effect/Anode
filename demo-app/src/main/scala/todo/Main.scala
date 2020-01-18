package todo

import earlyeffect.{VNode, log}
import org.scalajs.dom

object Main {

  object Router {

    def route: VNode =
      dom.window.location.pathname match {
        case "/b" =>
          b.App.TodoListApp
        case x =>
          a.App
      }
  }

  def main(args: Array[String]): Unit =
    earlyeffect.preact.render(Router.route, dom.document.body)
}
