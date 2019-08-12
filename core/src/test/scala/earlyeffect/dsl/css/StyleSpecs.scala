package earlyeffect.dsl.css

import earlyeffect._
import earlyeffect.dsl.css.values.rgb
import org.scalatest.{FlatSpec, Matchers}

class StyleSpecs extends FlatSpec with Matchers {
  import earlyeffect.dsl.css.Styles._
  val fooClass    = Selector(".foo", S.color("red"))
  val barClass    = Selector(".bar", S.color("blue"))
  val allChildren = Selector(" *", S.color("black"))

  val nested = Selector(
    ".foo",
    S.zIndex(0),
    Selector(".bar", S.zIndex(1), Selector(".baz", S.zIndex(2), Selector(".zooks", S.zIndex(3))))
  )
  "Simple selectors" should "make correct declarations" in {
    fooClass.toString should be(""".foo {
        |  color: red;
        |}
        |""".stripMargin)
  }
  "complex selectors" should "make correct declarations" in {
    val s = Selector(".baz", S.zIndex(1), fooClass, barClass, allChildren)
    s.toString should be(""".baz {
                           |  z-index: 1;
                           |}
                           |.baz.foo {
                           |  color: red;
                           |}
                           |
                           |.baz.bar {
                           |  color: blue;
                           |}
                           |
                           |.baz * {
                           |  color: black;
                           |}
                           |""".stripMargin)
  }
  "Nested selectors" should "make correct declarations" in {
    nested.toString should be(
      """.foo {
        |  z-index: 0;
        |}
        |.foo.bar {
        |  z-index: 1;
        |}
        |.foo.bar.baz {
        |  z-index: 2;
        |}
        |.foo.bar.baz.zooks {
        |  z-index: 3;
        |}
        |""".stripMargin
    )
  }

  "CssClass" should "support keyframes" in {
    val c = Css("a").Class(
      "foo",
      S.animationDuration.s(3),
      S.animationDelay.s(.5),
      S.animationTimingFunction("ease-in-out"),
      S.backgroundColor.rgba(255, 255, 113, 0),
      KeyFrames(
        "baz",
        KeyframeSelector("0%", backgroundColor.rgba(255, 255, 113, 0)),
        KeyframeSelector("50%", backgroundColor.rgba(255, 255, 113, 0.95)),
        KeyframeSelector("100%", backgroundColor.rgba(255, 255, 113, 0))
      )
    )
    c.mkString should be(s""".${c.className} {
        |  animation-duration: 3s;
        |  animation-delay: 0.5s;
        |  animation-timing-function: ease-in-out;
        |  background-color: rgb(255,255,113,0);
        |  animation-name: ${c.className}-baz;
        |}
        |@keyframes ${c.className}-baz {
        |  0% {
        |    background-color: rgb(255,255,113,0);
        |  }
        |  50% {
        |    background-color: rgb(255,255,113,0.95);
        |  }
        |  100% {
        |    background-color: rgb(255,255,113,0);
        |  }
        |}
        |""".stripMargin)
  }
  "prefixes" should "work" in {
    S.alignItems.safe.end.toString should be("align-items: safe end;")
  }
  "simple String declarations" should "work" in {
    S("foo")("bar").toString should be("foo: bar;")
    S("foo", "bar").toString should be("foo: bar;")
  }
  "declaration constructors with apply functions" should "work" in {
    S.borderColor(rgb(1, 2, 3)).toString should be("border-color: rgb(1,2,3);")
  }
}
