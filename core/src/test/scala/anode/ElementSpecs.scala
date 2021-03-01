package anode

import org.scalajs.dom
import munit._

import scala.scalajs.js

class ElementSpecs extends FunSuite with AnodeOps {
  test("Elements without attributes or children render") {
    render(E.span())
    check("<span></span>")
  }
  test("Elements with attributes but no children should render") {
    render(E.span(A.`class`("a"), A.id("foo")))
    check("""<span class="a" id="foo"></span>""")
  }
  test("Elements with None attributes but no children should render") {
    val a: Option[Attribute] = None
    render(E.span(A.`class`("a"), A.id("foo"), a))
    check("""<span class="a" id="foo"></span>""")
  }
  test("Elements with Some(a) attributes but no children shouldrender"){
    val a: Option[Attribute] = Some(A.id("foo"))
    render(E.span(A.`class`("a"), a))
    check("""<span class="a" id="foo"></span>""")
  }

  test("Elements with attributes and children shouldrender"){
    render(E.span(A.id("foo"), A.`class`("a"), E.button()))
    check("""<span class="a" id="foo"><button></button></span>""")
    render(
      E.span(A.id("foo"), A.`class`("a"), E.button(), E.a(A.href("foo.com")))
    )
    check(
      """<span class="a" id="foo"><button></button><a href="foo.com"></a></span>"""
    )
  }
  test("Elements with children but no attributes shouldrender"){
    render(E.span(E.span("hello"), E.span("world")))
    check(
      """<span id=""><span>hello</span><span>world</span></span>"""
    )
  }
  test("Elements with children as a sequence shouldrender"){
    render(E.span(Seq(E.span("hello"), E.span("world"))))
    check(
      """<span id=""><span>hello</span><span>world</span></span>"""
    )
  }
  test("Elements with a sequence of attributes and children shouldrender"){
    render(E.span(A.`class`("bar"), args(A.id("foo"), E.span("hello"), E.span("world"))))
    check(
      """<span id="foo" class="bar"><span>hello</span><span>world</span></span>"""
    )
  }
  test("Element with declarations shouldget a style attribute"){
    render(E.span(S.margin.px(10), S.color.rgb(100, 100, 100)))
    check("""<span id="" style="margin: 10px; color: rgb(100, 100, 100);"></span>""")
  }
  test("Element with declarations and a style attr shouldget a single style attribute"){
    val s = A.style(S.backgroundColor("white"))
    render(E.span(s, S.margin.px(10), S.color.rgb(100, 100, 100)))
    check("""<span id="" style="margin: 10px; color: rgb(100, 100, 100); background-color: white;"></span>""")
  }
  test("Elements shouldtake a key"){
    Assertions.assertEquals(E.span("foo").withKey("bar").vnode.key.toString,"bar")
  }
  test("Elements shouldtake a ref function and a key"){
    var effect: Boolean                    = false
    val f: js.Function1[dom.Element, Unit] = e => effect = true
    val n                                  = E.span("foo").withRef(f).withKey("foo")
    render(n)
    assert(n.vnode.key.contains("foo"))
    assert(effect)
  }
}
