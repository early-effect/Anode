package todo.a

import earlyeffect.{A, Component, E, VNode}
import org.scalajs.dom.raw.HTMLInputElement
import todo._
import todo.model.TodoList.actions.Add
import todo.model.{ModelCircuit, Todo}

object Creator extends Component[Unit] {
  override def render(props: Unit): VNode =
    E.input(
      A.id("new-todo"),
      A.`class`("new-todo"),
      A.placeholder("What needs to be done?"),
      A.onKeyDown(x => {
        val e = x.target.asInstanceOf[HTMLInputElement]
        if (x.keyCode == ENTER && e.value.nonEmpty) {
          ModelCircuit(Add(Todo(e.value)))
          e.value = ""
        }
      })
    )
}
