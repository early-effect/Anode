package todo.model

import diode.{Action, ActionHandler, ActionResult, ModelRW}

case class TodoList(todos: Seq[Todo], filter: Filter, online: Boolean) {
  lazy val countIncomplete: Int    = todos.count(!_.complete)
  def delete(todo: Todo): TodoList = copy(todos = todos.filter(_.key != todo.key))
  def add(todo: Todo): TodoList    = copy(todos = todos :+ todo)
  def update(todo: Todo): TodoList = copy(todos = todos.updated(todos.indexWhere(_.key == todo.key), todo))

  def finishEditing(todo: Todo, description: String): TodoList =
    update(todo.copy(description = description, editing = false))

  def cancelEditing(todo: Todo): TodoList = update(todo.copy(editing = false))
  def complete(todo: Todo): TodoList      = update(todo.copy(complete = true))
  def clearCompleted: TodoList            = copy(todos = todos.filterNot(_.complete))
  def setAll(comlete: Boolean): TodoList  = copy(todos = todos.map(x => x.copy(complete = comlete)))

  def filtered: Seq[Todo] = {
    import Filter._
    filter match {
      case All       => todos
      case Completed => todos.filter(_.complete)
      case Active    => todos.filterNot(_.complete)
    }
  }
}

sealed trait Filter

object Filter {
  case object All       extends Filter
  case object Completed extends Filter
  case object Active    extends Filter
}

object TodoList {

  case class Handler(m: ModelRW[Root, TodoList]) extends ActionHandler(m) {
    import actions._

    override protected def handle: PartialFunction[Any, ActionResult[Root]] = {
      case GoOnline                         => updated(value.copy(online = true))
      case GoOffline                        => updated(value.copy(online = false))
      case Add(todo)                        => updated(value.add(todo))
      case Delete(todo)                     => updated(value.delete(todo))
      case Update(todo)                     => updated(value.update(todo))
      case ApplyFilter(f)                   => updated(value.copy(filter = f))
      case ClearCompleted                   => updated(value.clearCompleted)
      case SetAll(complete)                 => updated(value.setAll(complete))
      case CancelEditing(todo)              => updated(value.cancelEditing(todo))
      case FinishEditing(todo, description) => updated(value.finishEditing(todo, description))
    }
  }

  object actions {
    final case object GoOnline                                      extends Action
    final case object GoOffline                                     extends Action
    final case class Add(todo: Todo)                                extends Action
    final case class Delete(todo: Todo)                             extends Action
    final case class Update(todo: Todo)                             extends Action
    final case class ApplyFilter(filter: Filter)                    extends Action
    final case object ClearCompleted                                extends Action
    final case class SetAll(boolean: Boolean)                       extends Action
    final case class FinishEditing(todo: Todo, description: String) extends Action
    final case class CancelEditing(todo: Todo)                      extends Action
  }
}
