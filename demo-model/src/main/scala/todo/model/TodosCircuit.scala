package todo.model

import diode.Circuit

object TodosCircuit extends Circuit[Root] {
  override protected def initialModel: Root = Root(TodoList(Seq.empty, Filter.All, false))

  override protected val actionHandler: HandlerFunction = TodoList.Handler(zoomTo(_.todoList))
}
