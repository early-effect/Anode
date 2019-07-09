package earlyeffect.dsl

import org.scalatest.{FlatSpec, Matchers}
import earlyeffect._

class StyleSpecs extends FlatSpec with Matchers {
  import Styles._
  val fooClass    = Selector(".foo", S.color("red"))
  val barClass    = Selector(".bar", S.color("blue"))
  val allChildren = Selector(" *", S.color("black"))

  val nested = Selector(
    ".foo",
    S.zIndex(0),
    Selector(".bar", S.zIndex(1), Selector(".baz", S.zIndex(2), Selector(".zooks", S.zIndex(3))))
  )
  "Simple selectors" should "make correct declarations" in {
    fooClass.mkString should be(""".foo {
        |  color: red;
        |}
        |""".stripMargin)
  }
  "complex selectors" should "make correct declarations" in {
    val s = Selector(".baz", S.zIndex(1), fooClass, barClass, allChildren)
    s.mkString should be(""".baz {
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
    nested.mkString should be(
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
}
