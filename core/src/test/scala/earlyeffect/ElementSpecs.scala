package earlyeffect

import earlyeffect.impl.Preact.AttributeOrChild
import org.scalajs.dom
import org.scalajs.dom.html.Document
import org.scalajs.dom.raw.Element
import org.scalatest.{FlatSpec, Matchers}

import scala.None

class ElementSpecs extends FlatSpec with Matchers {
  import ElementSpecs._
  def check(s: String): Unit = div.children(0).outerHTML should be(s)
  "Elements without attributes or children" should "render" in {
    render(E.span())
    check("<span></span>")
  }
  "Elements with attributes but no children" should "render" in {
    render(E.span(A.`class`("a"), A.id("foo")))
    check("""<span class="a" id="foo"></span>""")
  }
  "Elements with None attributes but no children" should "render" in {
    val a: Option[Attribute] = None
    render(E.span(A.`class`("a"), A.id("foo"), a))
    check("""<span class="a" id="foo"></span>""")
  }
  "Elements with Some(a) attributes but no children" should "render" in {
    val a: Option[Attribute] = Some(A.id("foo"))
    render(E.span(A.`class`("a"), a))
    check("""<span class="a" id="foo"></span>""")
  }

  "Elements with attributes and children" should "render" in {
    render(E.span(A.id("foo"), A.`class`("a"), E.button()))
    check("""<span class="a" id="foo"><button></button></span>""")
    render(
      E.span(A.id("foo"), A.`class`("a"), E.button(), E.a(A.href("foo.com")))
    )
    check(
      """<span class="a" id="foo"><button></button><a href="foo.com"></a></span>"""
    )
  }
  "Elements with children but no attributes" should "render" in {
    render(E.span(E.span("hello"), E.span("world")))
    check(
      """<span class="" id=""><span>hello</span><span>world</span></span>"""
    )
  }
  "Elements with children as a sequence" should "render" in {
    render(E.span(Seq(E.span("hello"), E.span("world"))))
    check(
      """<span class="" id=""><span>hello</span><span>world</span></span>"""
    )
  }
  "Elements with a sequence of attributes and children" should "render" in {
    render(E.span(A.`class`("bar"), args(A.id("foo"), E.span("hello"), E.span("world"))))
    check(
      """<span class="bar" id="foo"><span>hello</span><span>world</span></span>"""
    )
  }
}

object ElementSpecs {
  def render(vn: VirtualNode): Unit = Preact.render(vn, div)
  val doc: Document                 = dom.document
  val div: Element                  = dom.document.createElement("div")
  def replacedNode: Element         = div.querySelector("span")
  doc.documentElement.appendChild(div)
}
