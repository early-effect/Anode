package todo

import earlyeffect.pwa.ProgressiveWebApp
import earlyeffect.{VNode, log}
import org.scalajs.dom
import org.scalajs.dom.Event
import todo.a.DumbApp
import todo.model.ModelCircuit
import todo.model.TodoList.actions.{GoOffline, GoOnline}

import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js

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

  override def checkIntervalMillis: Int = 5 * 60 * 1000

  override def serviceWorkerUpdated(): Unit = {
    val o = Option(dom.window.localStorage.getItem("bread-crumb"))
    dom.window.localStorage.setItem("bread-crumb", "rye")
    log("o", o.getOrElse(""))
    o.foreach(_ => {
      dom.window.alert("hey must reload!")
      dom.window.location.reload(true)
    })
    log("set bread crumb..")
  }

  object Router {

    def route: VNode =
      if (dom.window.location.pathname == "/b") b.App.TodoListApp else a.App
  }

  def main(args: Array[String]): Unit = {
    if (isOnline) ModelCircuit(GoOnline) else ModelCircuit(GoOffline)
//    earlyeffect.preact.render(Router.route, dom.document.body)
    earlyeffect.preact.render(DumbApp(()), dom.document.body)
  }
}
