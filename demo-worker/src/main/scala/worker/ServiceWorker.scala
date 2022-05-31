package worker

import org.scalajs.dom.ServiceWorkerGlobalScope.{self => worker}
import org.scalajs.dom.{ExtendableEvent, FetchEvent}
import org.scalajs.dom.{Fetch, RequestInfo, Response}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.{Promise, |}

object ServiceWorker {
  type Resp = Response | Promise[Response]

  object cacheKeys {
    val App = "app"
  }

  def prepareCache: Future[Unit] = {
    println("preparing caches")

    (for {
      cacheStorage <- worker.caches.toOption
    } yield cacheStorage
      .delete(cacheKeys.App)
      .toFuture
      .flatMap(_ =>
        cacheStorage.open(cacheKeys.App).toFuture.flatMap { cache =>
          cache.addAll(Details.cachedAssets.toJSArray.map(_.asInstanceOf[RequestInfo])).toFuture
        }
      )).getOrElse(Future.failed(new Exception("cache storage not available")))
  }

  def request(info: RequestInfo): Future[Response] =
    worker.caches
      .map(
        _.`match`(info).toFuture
          .flatMap[Response] {
            case r: Response => Future(r)
            case _           => Fetch.fetch(info).toFuture
          }
      )
      .getOrElse(Future.failed(new Exception("cache storage not available")))

  def main(args: Array[String]): Unit = {

    worker.addEventListener(
      "install",
      (e: ExtendableEvent) => {
        worker.skipWaiting()
        e.waitUntil(prepareCache.toJSPromise)
      },
    )
    worker.addEventListener("fetch", (e: FetchEvent) => e.respondWith(request(e.request).asInstanceOf[Resp]))
  }
}
