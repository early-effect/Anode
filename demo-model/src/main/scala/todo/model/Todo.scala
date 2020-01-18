package todo.model

import java.util.UUID

case class Todo(key: String, description: String, complete: Boolean, editing: Boolean)

object Todo {

  def apply(description: String) =
    new Todo(UUID.randomUUID().toString, description, complete = false, editing = false)

}
