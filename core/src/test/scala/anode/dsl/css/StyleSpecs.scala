package anode.dsl.css

import anode._
import anode.dsl.css.values.rgb
import munit._

class StyleSpecs extends FunSuite {
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
          KeyframeSelector("100%", backgroundColor.rgba(255, 255, 113, 0)),
        ),
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
            S("visibility", "hidden").important,
          ),
        ),
      )

  val fooClass    = Selector(".foo", S.color("red"))
  val barClass    = Selector(".bar", S.color("blue"))
  val allChildren = Selector(" *", S.color("black"))

  val nested = Selector(
    ".foo",
    S.zIndex(0),
    Selector(".bar", S.zIndex(1), Selector(".baz", S.zIndex(2), Selector(".zooks", S.zIndex(3)))),
  )
  test("Simple selectors should make correct declarations") {
    assertEquals(
      fooClass.toString,
      (""".foo {
        |  color: red;
        |}
        |""".stripMargin),
    )
  }
  test("complex selectors should make correct declarations") {
    val s = Selector(".baz", S.zIndex(1), fooClass, barClass, allChildren)
    assertEquals(
      s.toString,
      """.baz {
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
                           |""".stripMargin,
    )
  }
  test("Nested selectors should make correct declarations") {
    assertEquals(
      nested.toString,
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
        |""".stripMargin,
    )
  }

  test("CssClass should support keyframes") {
    assertEquals(
      MyCLass1.mkString,
      s""".${MyCLass1.className} {
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
        |""".stripMargin,
    )
  }
  test("Css#Class support media queries") {
    assertEquals(
      MyClass2.mkString,
      s"""${MyClass2.selector} {
                           |  min-height: 100%;
                           |}
                           |@media print {
                           |div.viewport * :after :before {
                           |  display: none !important;
                           |  opacity: 0 !important;
                           |  visibility: hidden !important;
                           |}
                           |}""".stripMargin,
    )
  }
  test("prefixes should work") {
    assertEquals(S.alignItems.safe.end.toString, "align-items: safe end;")
  }
  test("simple String declarations should work") {
    assertEquals(S("foo")("bar").toString, "foo: bar;")
    assertEquals(S("foo", "bar").toString, "foo: bar;")
  }
  test("declaration constructors with apply functions work") {
    assertEquals(S.borderColor(rgb(1, 2, 3)).toString, "border-color: rgb(1,2,3);")
  }
  test("custom properties should produce valid CSS") {
    assertEquals(S.background.customProperty("foo", "green").toString, "background: var(--foo, green);")
    assertEquals(S.background.customProperty("foo").toString, "background: var(--foo);")
  }
}
