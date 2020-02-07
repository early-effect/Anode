package todo

import earlyeffect.pwa.ProgressiveWebApp
import earlyeffect.{VNode, log}
import org.scalajs.dom
import org.scalajs.dom.Event
import todo.model.ModelCircuit
import todo.model.TodoList.actions.{GoOffline, GoOnline}

object Main extends ProgressiveWebApp {

  override def goOnline(f: Event): Unit = {
    ModelCircuit(GoOnline)
    log("going online", f)
  }

  override def goOffline(f: Event): Unit = {
    ModelCircuit(GoOffline)
    log("going offline", f)
  }

  override def serviceWorkerPath: String = "worker.js"

  override def serviceWorkerUpdated(): Unit = {
    dom.window.alert("hey must reload!")
    dom.window.location.reload()
  }

  object Router {

    def route: VNode =
      dom.window.location.pathname match {
        case "/b" =>
          b.App.TodoListApp
        case x =>
          a.App
      }
  }

  def main(args: Array[String]): Unit = {
    if (isOnline) ModelCircuit(GoOnline) else ModelCircuit(GoOffline)
    earlyeffect.preact.render(Router.route, dom.document.body)
  }
}
