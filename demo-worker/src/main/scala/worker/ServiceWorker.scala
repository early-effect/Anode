package worker

import org.scalajs.dom.experimental.serviceworkers.ServiceWorkerGlobalScope.self
import org.scalajs.dom.experimental.serviceworkers.{ExtendableEvent, FetchEvent}
import org.scalajs.dom.experimental.{Fetch, RequestInfo, Response}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.{Promise, |}

object ServiceWorker {
  type Resp = Response | Promise[Response]

  object cacheKeys {
    val App = "app"
  }

  def prepareCache: Future[Unit] = {
    println("preparing caches")
    self.caches
      .delete(cacheKeys.App)
      .toFuture
      .map(_ =>
        self.caches.open(cacheKeys.App).toFuture.flatMap { cache =>
          cache.addAll(Details.cachedAssets.toJSArray.map(_.asInstanceOf[RequestInfo])).toFuture
        }
      )
  }

  def request(info: RequestInfo): js.Promise[Response] =
    self.caches
      .`match`(info)
      .toFuture
      .flatMap[Response] {
        case r: Response =>
          Future(r)
        case _ =>
          Fetch.fetch(info).toFuture
      }
      .toJSPromise

  def main(args: Array[String]): Unit = {

    self.addEventListener(
      "install",
      (e: ExtendableEvent) => {
        self.skipWaiting()
        e.waitUntil(prepareCache.toJSPromise)
      },
    )
    self.addEventListener("fetch", (e: FetchEvent) => e.respondWith(request(e.request).asInstanceOf[Resp]))
  }
}
