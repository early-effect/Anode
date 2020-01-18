package todo.a

import diode.ModelR
import earlyeffect.{A, E, VNode}
import todo.model.TodoList.actions.{ApplyFilter, ClearCompleted}
import todo.model._

object Footer extends TodoComponent[Unit, TodoList] {
  def change(f: Filter) = ModelCircuit(ApplyFilter(f))

  override def render(props: Unit, todoList: TodoList): VNode = {

    val s = if (todoList.countIncomplete > 1) " items left" else " item left"

    E.footer(
      A.`class`("footer"),
      E.span(A.`class`("todo-count"), E.strong(todoList.countIncomplete), E.span(s)),
      E.ul(
        A.`class`("filters"),
        E.li("All", A.`class`("selected").when(todoList.filter == All), A.onClick(_ => change(All))),
        E.li("Active", A.`class`("selected").when(todoList.filter == Active), A.onClick(_ => change(Active))),
        E.li("Completed", A.`class`("selected").when(todoList.filter == Completed), A.onClick(_ => change(Completed)))
      ),
      E.button(A.`class`("clear-completed"), "Clear completed", A.onClick(_ => {
        ModelCircuit(ClearCompleted)
      }))
    )

  }

  override def modelReader(p: Unit): ModelR[Root, TodoList] = circuit.zoom(_.todoList)
}
