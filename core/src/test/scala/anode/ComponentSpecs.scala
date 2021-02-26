package anode

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
      override def render(props: String): VNode = E.span(props)
    }
    render(Simple("foo"))
    check("<span>foo</span>")
  }
  "A Component with a string prop and an instance selector and a class selector" should "render" in {
    object Simple extends Component[String] with InstanceDataSelector with ClassSelector {

      override def extractAttributeValue(instance: Simple.Instance): String = instance.props

      override def render(props: String): VNode = E.span(props)
    }
    render(Simple("foo"))
    check(
      "<span data-earlyeffect-earlyeffect-componentspecs-simple-5=\"foo\" class=\"earlyeffect-ComponentSpecs-Simple-5\">foo</span>"
    )
  }
  "A didCatch() error boundary" should "render an error condition" in {
    object Catcher extends StatefulComponent[String, Option[js.Error]] {
      override def initialState(name: String): Option[js.Error] = None

      val SuperBorkedChildComponent: Component[String] = (name: String) => {
        parent.removeChild(parent) // this will throw an Exception
        E.span(name)
      }

      override def render(name: String, error: Option[js.Error], instance: Catcher.Instance): VNode =
        error.fold(SuperBorkedChildComponent(name))(e => E.div(e.message))

      override def didCatch(e: js.Error, instance: Catcher.Instance): Unit = instance.setState(Some(e))
    }
    render(Catcher("foo"))
    checkAfter(10)(
      "<div>The node to be removed is not a child of this node.</div>"
    ) // check async so we can get the final re-render
  }
}
