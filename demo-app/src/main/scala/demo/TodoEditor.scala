package demo

import demo.model.TodoList.actions.Update
import demo.model.{ModelCircuit, Todo}
import earlyeffect.Component
import org.scalajs.dom.raw.HTMLInputElement
import earlyeffect.core.{A, E, VirtualNode}

case class TodoEditor(todo: Todo) extends Component[TodoEditor] {
  override def render(props: TodoEditor): VirtualNode =
    E.input(
      A.id(s"editor-${todo.key}"),
      A.`class`("edit"),
      A.value(todo.description)
    )
  override def didMount(instance: I): Unit = {
    val e              = instance.rawBase.asInstanceOf[HTMLInputElement]
    def update(): Unit = ModelCircuit(Update(todo.copy(editing = false, description = e.value.trim)))
    def clearThen(f: => Unit): Unit = {
      e.onblur = _ => ()
      f
    }
    e.onblur = _ => update()
    e.onkeydown = k => {
      if (k.keyCode == ESCAPE) clearThen {
        ModelCircuit(Update(todo.copy(editing = false)))
      } else if (k.keyCode == ENTER) clearThen {
        update()
      }
    }
    e.focus()
  }
}
