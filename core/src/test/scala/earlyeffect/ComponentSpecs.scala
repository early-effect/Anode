package earlyeffect

import org.scalatest.{FlatSpec, Matchers}

class ComponentSpecs extends FlatSpec with Matchers with EarlyOps {
  "Components" should "render" in {
    object Simple extends Component[Unit] {
      override def render(props: Unit): VNode =
        E.span("foo")
    }
    render(Simple)
    parent.innerHTML should be("<span>foo</span>")
  }
  "A Component with a string prop" should "render" in {
    object Simple extends Component[String] {
      override def render(props: String): VNode =
        E.span(props)
    }
    render(Simple("foo"))
    parent.innerHTML should be("<span>foo</span>")
  }
}
