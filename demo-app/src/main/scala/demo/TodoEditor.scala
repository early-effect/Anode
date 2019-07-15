package demo

import demo.model.TodoList.actions.Update
import demo.model.{ModelCircuit, Todo}
import earlyeffect._
import org.scalajs.dom.raw.HTMLInputElement

import scala.scalajs.js.timers

object TodoEditor extends Component[Todo] {
  override def render(todo: Todo): VNode =
    E.input(
        A.id(s"editor-${todo.key}"),
        A.`class`("edit"),
        A.value(todo.description)
      )
      .withRef(x => {
        val e              = x.asInstanceOf[HTMLInputElement]
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
        timers.setTimeout(2)(e.focus())
      })
}
