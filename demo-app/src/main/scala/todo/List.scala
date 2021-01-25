package todo

import diode.ModelR
import earlyeffect._
import earlyeffect.dsl.css.CssClass
import org.scalajs.dom.raw.HTMLInputElement
import todo.model.TodoList.actions.SetAll
import todo.model.{Root, Todo, TodosCircuit}

object List extends TodoComponent[Unit, Seq[Todo]] with ClassSelector {
  override def modelReader(p: Unit): ModelR[Root, Seq[Todo]] = zoom(_.todoList.filtered)

  object css {
    import S._

    object Main
        extends CssClass(
          position.relative,
          zIndex(2),
          borderTop("1px solid #e6e6e6"),
        )

    object ToggleAll
        extends CssClass(
          textAlign.center,
          border.none,
          opacity(0),
          position.absolute,
          Selector(
            " + label",
            cursor.pointer,
            width.px(60),
            height.px(34),
            fontSize.zero,
            position.absolute,
            top.px(-52),
            left.px(-13),
            transform("rotate(90deg)"),
          ),
          Selector(
            " + label:before",
            content("'❯'"),
            fontSize.px(22),
            padding("10px 27px 10px 27px"),
          ),
        )

    object List
        extends CssClass(
          listStyle.none,
          Selector(" li:last-child", borderBottom("none")),
        )
  }

  override def render(props: Unit, todos: Seq[Todo]): VNode =
    E.section(
      css.Main,
      E.input(
        css.ToggleAll,
        A.id("toggle-all"),
        A.`type`("checkbox"),
        A.checked(todos.forall(_.complete)),
        A.onChange { e =>
          TodosCircuit(SetAll(e.target.asInstanceOf[HTMLInputElement].checked))
        },
      ),
      E.label(
        A.`for`("toggle-all"),
        if (todos.nonEmpty && todos.forall(_.complete)) S.color("#000000") else S.color("#e6e6e6"),
      ),
      E.ul(
        css.List,
        S.margin.zero,
        S.padding.zero,
        todos.map(x => Item(x).withKey(x.key)),
      ),
    )
}
