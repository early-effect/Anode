package earlyeffect

import org.scalatest.flatspec.AnyFlatSpec

import scala.scalajs.js

class ComponentSpecs extends AnyFlatSpec with EarlyOps {
  "Components" should "render" in {
    object Simple extends Component[Unit] {
      override def render(props: Unit): VNode =
        E.div(
          js.Array(E.div("foo"), E.div("bar"))
        )
    }
    render(Simple)
    check("<div><div>foo</div><div>bar</div></div>")
  }
  "A Component with a string prop" should "render" in {
    object Simple extends Component[String] {
      override def render(props: String): VNode =
        E.span(props)
    }
    render(Simple("foo"))
    check("<span>foo</span>")
  }
}
