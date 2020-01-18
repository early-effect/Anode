package todo.a

import diode.ModelR
import earlyeffect.{A, ClassSelector, E, VNode}
import org.scalajs.dom.raw.HTMLInputElement
import todo.model.{ModelCircuit, Root, Todo}
import todo.model.TodoList.actions.SetAll

object List extends TodoComponent[Unit, Seq[Todo]] with ClassSelector {
  override def modelReader(p: Unit): ModelR[Root, Seq[Todo]] = zoom(_.todoList.filtered)

  override def render(props: Unit, todos: Seq[Todo]): VNode =
    E.section(
      A.`class`("main"),
      E.input(
        A.id("toggle-all"),
        A.`class`("toggle-all"),
        A.`type`("checkbox"),
        A.checked(todos.forall(_.complete)),
        A.onChange(e => {
          ModelCircuit(SetAll(e.target.asInstanceOf[HTMLInputElement].checked))
        })
      ),
      E.label(A.`for`("toggle-all")),
      E.ul(
        A.`class`("todo-list"),
        todos.map(x => Item(x).withKey(x.key))
      )
    )
}
