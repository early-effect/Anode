package todo

import anode.S._
import anode._
import org.scalajs.dom.raw.HTMLInputElement
import todo.model.TodoList.actions.Add
import todo.model.{Todo, TodosCircuit}

object Creator extends StatefulComponent[Unit, Boolean] {

  object Input
      extends StyledElement(E.input)(
        position.relative,
        margin.zero,
        width.pct(100),
        fontSize.px(24),
        fontFamily.inherit,
        fontWeight.inherit,
        lineHeight.em(1.4),
        color.inherit,
        boxSizing.borderBox,
        borderBottom("1px solid #ededed"),
        padding("16px 16px 16px 60px"),
        border.none,
        background.rgba(0, 0, 0, 0.05),
        Selector("::placeholder", fontStyle.italic, fontWeight(300), color.rgba(75, 25, 25, .5)),
      )

  override def initialState(props: Unit): Boolean = true

  override def render(props: Unit, state: Boolean, instance: Creator.Instance): VNode =
    Input(
//      A("readonly", state),
      A.placeholder("What needs to be done?"),
      A.onMouseDown(_ => instance.setState(false)),
//      A.onClick(_ => instance.setState(false)),
      A.onKeyDown { x =>
        val e = x.target.asInstanceOf[HTMLInputElement]
        if (x.keyCode == ENTER && e.value.nonEmpty) {
          TodosCircuit(Add(Todo(e.value)))
          e.value = ""
        }
      },
    )
//      .withKey(s"input-$state")

}
