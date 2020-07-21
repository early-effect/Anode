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

  override def checkIntervalMillis: Int = 1 * 60 * 1000

  override def serviceWorkerUpdated(): Unit = {
    dom.window.alert("Hey must reload! Now!!! Silly!")
    dom.window.location.reload(true)
  }

  object Router {

    def route: VNode =
      if (dom.window.location.pathname == "/b") b.App.TodoListApp else a.App
  }

  def main(args: Array[String]): Unit = {
    if (isOnline) ModelCircuit(GoOnline) else ModelCircuit(GoOffline)
    earlyeffect.preact.render(Router.route, dom.document.body)
  }
}
