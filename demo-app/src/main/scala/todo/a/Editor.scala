package todo.a
import todo._

import todo.model.TodoList.actions.Update
import todo.model.{ModelCircuit, Todo}
import earlyeffect._
import org.scalajs.dom.raw.HTMLInputElement

import scala.scalajs.js.timers

object Editor extends StatefulComponent[Todo, String] with InstanceDataSelector with ClassSelector {

  override def extractAttributeValue(instance: ComponentInstance): String =
    instance.props.key

  override def initialState(t: Todo): String = t.description

  override def render(props: Todo, state: String, instance: ComponentInstance): VNode = {
    def update(): Unit = ModelCircuit(Update(props.copy(editing = false, description = state)))
    E.input(
        A.`class`("edit"),
        A.value(state),
        A.onKeyDown { k =>
          if (k.keyCode == ESCAPE) ModelCircuit(Update(props.copy(editing = false)))
          else if (k.keyCode == ENTER) update()
        },
        A.onKeyUp(_ => instance.setState(instance.base.asInstanceOf[HTMLInputElement].value)),
        A.onBlur { _ =>
          ModelCircuit
            .zoom(_.todoList.todos.find { x =>
              x.key == props.key && x.editing
            })
            .value
            .foreach(_ => update())
        }
      )
      .withRef(x => timers.setTimeout(1)(x.asInstanceOf[HTMLInputElement].focus()))
  }
}
