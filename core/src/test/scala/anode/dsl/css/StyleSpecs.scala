package anode.dsl.css

import anode._
import anode.dsl.css.values.rgb
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.scalajs.js

class StyleSpecs extends AnyFlatSpec with Matchers {
  import anode.dsl.css.Styles._

  object MyCLass1
      extends CssClass(
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

  object MyClass2
      extends CssClass(
        S.minHeight.percent(100),
        MediaQuery(
          "print",
          Selector(
            "div.viewport * :after :before",
            S.display.none.important,
            opacity(0).important,
            S("visibility", "hidden").important
          )
        )
      )

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
    MyCLass1.mkString should be(s""".${MyCLass1.className} {
        |  animation-duration: 3s;
        |  animation-delay: 0.5s;
        |  animation-timing-function: ease-in-out;
        |  background-color: rgba(255,255,113,0);
        |  animation-name: ${MyCLass1.className}-baz;
        |}
        |@keyframes ${MyCLass1.className}-baz {
        |  0% {
        |    background-color: rgba(255,255,113,0);
        |  }
        |  50% {
        |    background-color: rgba(255,255,113,0.95);
        |  }
        |  100% {
        |    background-color: rgba(255,255,113,0);
        |  }
        |}
        |""".stripMargin)
  }
  "Css#Class" should "support media queries" in {
    MyClass2.mkString should be(s"""${MyClass2.selector} {
                           |  min-height: 100%;
                           |}
                           |@media print {
                           |div.viewport * :after :before {
                           |  display: none !important;
                           |  opacity: 0 !important;
                           |  visibility: hidden !important;
                           |}
                           |}""".stripMargin)
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
  "custom properties" should "produce valid CSS" in {
    S.background.customProperty("foo", "green").toString should be("background: var(--foo, green);")
    S.background.customProperty("foo").toString should be("background: var(--foo);")
  }
}
