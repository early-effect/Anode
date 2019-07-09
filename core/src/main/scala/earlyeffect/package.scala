import earlyeffect.dsl._
import earlyeffect.impl.Preact._
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLCollection

import scala.language.implicitConversions
import scala.scalajs.js

package object earlyeffect {
  val Preact: impl.Preact.type = impl.Preact

  type VirtualNode = impl.VirtualNode

  type ComponentFunction[T] = T => VirtualNode

  type A = Attributes.type
  val A: A = Attributes

  type E = Elements.type
  val E: E = Elements

  type S = dsl.Styles.type
  val S: S = dsl.Styles

  dom.Event
  def log(m: js.Any, a: Any*): Unit = dom.window.console.log(m, a.map(_.asInstanceOf[js.Any]): _*)

  def fragment(children: Child*): VirtualNode = h(Fragment, null, children: _*)

  object implicits {

    implicit def attrsToDictionary(as: NamedNodeMap): AnyDictionary = {
      val res: AnyDictionary = js.Dictionary[js.Any]()
      for (n <- 0 until as.length) {
        val attr: Attr = as(n)
        res.update(attr.name, attr.value)
      }
      res
    }
    implicit def htmlCollectionToSeq(c: HTMLCollection): Seq[Child] = {
      val res = new js.Array[Child](c.length)
      for (n <- 0 until c.length) {
        res(n) = c(n)
      }
      res.toSeq
    }
    implicit def textToChild(t: dom.Text): Child = t.data
    implicit def elementToPreactElement(e: dom.Element): Child =
      h(e.tagName, e.attributes, e.children: _*)
  }

  def f(node: FunctionalComponent)(params: AnyDictionary): VirtualNode =
    h(node, params)

  implicit def f[T](cf: ComponentFunction[T])(t: T): VirtualNode =
    f(cf: FunctionalComponent)(
      toDictionary(t.asInstanceOf[js.Any])
    )

  implicit def toPFC[T](cf: ComponentFunction[T]): FunctionalComponent =
    (d: js.Dynamic) => {
      cf(d.p1.asInstanceOf[T])
    }

  def toDictionary[T](t: T): js.Dictionary[js.Any] =
    js.Dictionary("p1" -> t.asInstanceOf[js.Any])

  val Empty: VirtualNode = null

  def when[T](p: => Boolean)(t: => T): T = if (p) t else null.asInstanceOf[T]

  implicit def optionToNode(o: Option[VirtualNode]): AttributeOrChild =
    o.fold(Empty)(x => {
      x
    })

  implicit class richDouble(d: Double) {
    def pct = s"$d%"
    def px  = s"${d}px"
    def em  = s"${d}em"
  }
  implicit class richInt(n: Int) {
    def pct = s"$n%"
    def px  = s"${n}px"
    def em  = s"${n}em"
  }

  implicit def toChildrenAsAttributeOrChild(s: Seq[VirtualNode]): AttributeOrChild = Children(s)
}
