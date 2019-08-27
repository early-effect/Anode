package earlyeffect

import org.scalatest.{FlatSpec, Matchers}

class ComponentSpecs extends FlatSpec with EarlyOps {
  "Components" should "render" in {
    object Simple extends Component[Unit] {
      override def render(props: Unit): VNode =
        E.span("foo")
    }
    render(Simple)
    check("<span>foo</span>")
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
