package earlyeffect.pwa

import org.scalajs.dom
import org.scalajs.dom.experimental.serviceworkers._
import org.scalajs.dom.raw.VisibilityState

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.timers.SetIntervalHandle
import scala.util.{Failure, Success}

trait ProgressiveWebApp { self =>

  private var onlineState: Boolean = dom.window.navigator.onLine

  private val navigator: ServiceWorkerNavigator = dom.window.navigator

  private var timer: UndefOr[SetIntervalHandle] = js.undefined

  /**
    * Override for if you want to register service worker for https://localhost
    * @return
    */
  def shouldRegister: Boolean = !(l.protocol == "https:" && l.hostname == "localhost")

  val l = dom.window.location
  if (shouldRegister)
    navigator.serviceWorker
      .register(serviceWorkerPath)
      .toFuture
      .onComplete {
        case Success(r) => serviceWorkerRegistered(r)
        case Failure(t) => handleRegistrationError(t)
      }

  dom.window.addEventListener("online", (e: dom.Event) => {
    onlineState = true
    self.goOnline(e)
  })

  dom.window.addEventListener("offline", (e: dom.Event) => {
    onlineState = false
    self.goOffline(e)
  })

  def isProgressiveWebApp: Boolean = dom.window.matchMedia("(display-mode: standalone)").matches

  final def isOnline: Boolean  = onlineState
  final def isOffline: Boolean = !isOnline

  def goOnline(event: dom.Event): Unit
  def goOffline(event: dom.Event): Unit

  def serviceWorkerPath: String

  def checkIntervalMillis = 1000 * 60 * 30

  def serviceWorkerUpdated(): Unit

  def update(r: ServiceWorkerRegistration): Unit = {
    r.onupdatefound = _ => {
      r.installing.onstatechange = _.target.asInstanceOf[ServiceWorker].state match {
        case "installed" => serviceWorkerUpdated()
        case _           =>
      }
    }
    r.update
  }

  def installTimer(r: ServiceWorkerRegistration): Unit = {
    dom.console.info(s"service worker timer installed.", isProgressiveWebApp, isOnline)
    timer.foreach(js.timers.clearInterval)
    timer = js.timers.setInterval(checkIntervalMillis)(
      update(r)
    )
    update(r)
  }

  def serviceWorkerRegistered(r: ServiceWorkerRegistration): Unit = {
    dom.document.addEventListener(
      "visibilitychange",
      (_: dom.Event) => {
        val s = dom.document.visibilityState.asInstanceOf[VisibilityState]
        if (s == VisibilityState.visible) {
          dom.console.info("Document became visible - checking for worker update")
          installTimer(r)
        }
      }
    )
    installTimer(r)
    r.onupdatefound = _ => {
      r.installing.onstatechange = _.target.asInstanceOf[ServiceWorker].state match {
        case "installed" => serviceWorkerUpdated()
        case _           =>
      }
    }
    dom.console.info(
      "Registered service worker.",
      if (isProgressiveWebApp) "Running as PWA." else "Running in browser.",
      if (isOnline) "Device is online." else "Device is offline."
    )
  }

  def handleRegistrationError(t: Throwable): Unit =
    dom.console.error("Failed to register service worker", t)

}
