package todo

import anode._
import anode.dsl.css.CssClass
import org.scalajs.dom.HTMLInputElement
import todo.model.TodoList.actions.{CancelEditing, FinishEditing}
import todo.model.{Todo, TodosCircuit}

import scala.scalajs.js.timers

object Editor extends StatefulComponent[Todo, String] with InstanceDataSelector with ClassSelector {

  override def extractAttributeValue(instance: Instance): String = instance.props.key

  override def initialState(t: Todo): String = t.description

  object css {
    import S._

    object Editing
        extends CssClass(
          font.inherit,
          border("1px solid #999"),
          position.relative,
          display.block,
          width.px(506),
          margin("0 0 0 43px"),
          fontSize.px(24),
          padding("12px 16px"),
          boxSizing.borderBox,
          boxShadow("inset 0 -1px 5px 0 rgba(0, 0, 0, 0.2)"),
        )
  }

  override def render(props: Todo, state: String, instance: Instance): VNode = {
    def finishEditing(): Unit = TodosCircuit(FinishEditing(props, state))
    def cancelEditing(): Unit = TodosCircuit(CancelEditing(props))
    E.input(
      css.Editing,
      A.value(state),
      A.onKeyDown { k =>
        k.keyCode match {
          case ESCAPE => cancelEditing()
          case ENTER  => finishEditing()
          case _      => ()
        }
      },
      A.onKeyUp(_ => instance.setState(instance.base.asInstanceOf[HTMLInputElement].value)),
      A.onBlur(_ => finishEditing()),
    ).withRef(x => timers.setTimeout(1)(x.asInstanceOf[HTMLInputElement].focus()))
  }
}
