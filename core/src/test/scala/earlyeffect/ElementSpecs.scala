package earlyeffect

import org.scalajs.dom
import org.scalatest.{FlatSpec, Matchers}

import scala.scalajs.js

class ElementSpecs extends FlatSpec with Matchers with EarlyOps {
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
  "Element with declarations" should "get a style attribute" in {
    render(E.span(S.margin.px(10), S.color.rgb(100, 100, 100)))
    check("""<span class="" id="" style="margin: 10px; color: rgb(100, 100, 100);"></span>""")
  }
  "Element with declarations and a style attr" should "get a single style attribute" in {
    val s = A.style(S.backgroundColor("white"))
    render(E.span(s, S.margin.px(10), S.color.rgb(100, 100, 100)))
    check("""<span class="" id="" style="margin: 10px; color: rgb(100, 100, 100); background-color: white;"></span>""")
  }
  "Elements" should "take a key" in {
    E.span("foo").withKey("bar").vnode.key should be("bar")
  }
  "Elements" should "take a ref function and a key" in {
    var effect: Boolean                    = false
    val f: js.Function1[dom.Element, Unit] = e => effect = true
    val n                                  = E.span("foo").withRef(f).withKey("foo")
    render(n)
    assert(n.vnode.key.contains("foo"))
    assert(effect)
  }
}
