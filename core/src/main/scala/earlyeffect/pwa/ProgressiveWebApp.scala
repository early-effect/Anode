package earlyeffect.pwa

import org.scalajs.dom
import org.scalajs.dom.experimental.serviceworkers._
import org.scalajs.dom.raw.VisibilityState

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.timers.SetIntervalHandle

trait ProgressiveWebApp { self =>
  import dom.window.location

  private var onlineState: Boolean             = dom.window.navigator.onLine
  private var timer: Option[SetIntervalHandle] = None

  private val navigator: ServiceWorkerNavigator = dom.window.navigator

  def shouldRegister: Boolean = !(location.protocol == "https:" && location.hostname == "localhost")

  def debug = false

  case object NoServiceWorkerSupport extends Throwable {
    override def getMessage: String = "No service worker support."
  }

  def register(): Unit =
    Option(navigator.serviceWorker).fold {
      handleRegistrationError(NoServiceWorkerSupport)
    } { workerContainer =>
      workerContainer
        .register(serviceWorkerPath)
        .toFuture
        .map(reg => {
          serviceWorkerRegistered(reg)
        })
        .recover(handleRegistrationError(_))
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
    r.onupdatefound = _ => {
      Option(r.installing)
        .orElse(Option(r.waiting))
        .foreach(
          worker =>
            worker.onstatechange = { _ =>
              if (worker.state == "installed") serviceWorkerUpdated()
            }
        )
    }
    r.update()
  }

  def installTimer(r: ServiceWorkerRegistration): Unit = {
    timer.foreach(js.timers.clearInterval)
    timer = Some(
      js.timers.setInterval(checkIntervalMillis)(
        update(r)
      )
    )
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
          installTimer(r)
          update(r)
        }
      }
    )
    installTimer(r)
  }

  def handleRegistrationError(t: Throwable): Unit =
    dom.console.error("Failed to register service worker", t)

}
