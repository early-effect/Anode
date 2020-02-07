package todo.model

import diode.Circuit

object ModelCircuit extends Circuit[Root] {
  override protected def initialModel: Root = Root(TodoList(Seq.empty, All, false))

  override protected val actionHandler: HandlerFunction = TodoList.Handler(zoomTo(_.todoList))
}
