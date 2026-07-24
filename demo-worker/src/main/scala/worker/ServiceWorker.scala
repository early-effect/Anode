package worker

import org.scalajs.dom.{
  CacheStorage,
  ExtendableEvent,
  Fetch,
  FetchEvent,
  RequestInfo,
  Response,
  ServiceWorkerGlobalScope,
}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.|

object ServiceWorker {
  private val self = ServiceWorkerGlobalScope.self

  type Resp = Response | js.Promise[Response]

  object cacheKeys {
    val App = "app"
  }

  private def caches: CacheStorage =
    self.caches.getOrElse(sys.error("CacheStorage is not available in this service worker scope"))

  def prepareCache: Future[Unit] = {
    println("preparing caches")
    caches
      .delete(cacheKeys.App)
      .toFuture
      .flatMap(_ =>
        caches.open(cacheKeys.App).toFuture.flatMap { cache =>
          cache.addAll(Details.cachedAssets.toJSArray.map(_.asInstanceOf[RequestInfo])).toFuture
        }
      )
      .map(_ => ())
  }

  def request(info: RequestInfo): js.Promise[Response] =
    caches
      .`match`(info)
      .toFuture
      .flatMap[Response] {
        case r: Response =>
          Future.successful(r)
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
