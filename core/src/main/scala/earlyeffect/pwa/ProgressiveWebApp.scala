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

  def register(): Unit =
    try {
      navigator.serviceWorker.register(serviceWorkerPath).toFuture.map { reg =>
        Option(reg.waiting).map(worker => {
          worker
        })
      }
      navigator.serviceWorker
        .register(serviceWorkerPath)
        .toFuture
        .onComplete {
          case Success(r) => serviceWorkerRegistered(r)
          case Failure(t) => handleRegistrationError(t)
        }
    } catch {
      case e: Throwable =>
        handleRegistrationError(e)
        dom.console.log("can't register service worker", e)
    }

  if (shouldRegister) register()

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
    dom.window.console.info("checking for updated worker.")
    r.onupdatefound = _ => {
      Option(r.installing)
        .orElse(Option(r.waiting))
        .foreach(
          worker =>
            worker.onstatechange = { _ =>
              dom.window.console.info("replacement worker found - calling updated")
              if (worker.state == "installed") serviceWorkerUpdated()
            }
        )
    }
    r.update
  }

  def installTimer(r: ServiceWorkerRegistration): Unit = {
    dom.console.info(
      s"Service worker timer installed: $checkIntervalMillis millis\n is PWA: $isProgressiveWebApp\n online: $isOnline"
    )
    timer.foreach(js.timers.clearInterval)
    timer = js.timers.setInterval(checkIntervalMillis)(
      update(r)
    )
    update(r)
  }

  def serviceWorkerRegistered(r: ServiceWorkerRegistration): Unit = {
    Option(r.waiting).foreach(_ => {
      Option(r.active).foreach(_ => serviceWorkerUpdated())
    })
    dom.document.addEventListener(
      "visibilitychange",
      (_: dom.Event) => {
        val s = dom.document.visibilityState.asInstanceOf[VisibilityState]
        if (s != VisibilityState.hidden) {
          dom.console.info("Checking for worker update because of window state change.")
          installTimer(r)
        }
      }
    )
    dom.console.info(
      "Registered service worker.",
      if (isProgressiveWebApp) "Running as PWA." else "Running in browser.",
      if (isOnline) "Device is online." else "Device is offline."
    )
    installTimer(r)
  }

  def handleRegistrationError(t: Throwable): Unit =
    dom.console.error("Failed to register service worker", t)

}
