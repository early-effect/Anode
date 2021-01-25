package todo

import diode.ModelR
import earlyeffect._
import earlyeffect.dsl.css.CssClass
import todo.model.TodoList.actions.{ApplyFilter, ClearCompleted}
import todo.model._

object Footer extends TodoComponent[Unit, TodoList] {
  def change(f: Filter) = TodosCircuit(ApplyFilter(f))

  object css {
    import S._

    object Main
        extends CssClass(
          color("#777"),
          padding("10px 15px"),
          height.px(20),
          textAlign.center,
          borderTop("1px solid #e6e6e6"),
          Selector(
            ":before",
            content("''"),
            position.absolute,
            right.zero,
            bottom.zero,
            left.zero,
            right.zero,
            height.px(50),
            overflow.hidden,
            boxShadow(
              "0 1px 1px rgba(0, 0, 0, 0.2), 0 8px 0 -3px #f6f6f6, 0 9px 1px -3px rgba(0, 0, 0, 0.2), 0 16px 0 -6px #f6f6f6,0 17px 2px -6px rgba(0, 0, 0, 0.2)"
            ),
          ),
        )

    object TodoCount
        extends CssClass(
          float.left,
          textAlign.left,
          Selector(" strong", font.inherit, fontWeight(300)),
        )

    object Filters
        extends CssClass(
          margin.zero,
          padding.zero,
          listStyle.none,
          position.absolute,
          right.zero,
          left.zero,
        )

    object ClearCompleted
        extends CssClass(
          float.right,
          position.relative,
          lineHeight.px(20),
          textDecoration.none,
          cursor.pointer,
          margin.zero,
          padding.zero,
          border.zero,
          background.none,
          fontSize.pct(100),
          verticalAlign.baseline,
          font.inherit,
          color.inherit,
          appearance.none,
        )
  }

  override def render(props: Unit, todoList: TodoList): VNode = {

    val s = if (todoList.countIncomplete > 1) " items left" else " item left"

    E.footer(
      css.Main,
      E.span(css.TodoCount, E.strong(todoList.countIncomplete), E.span(s)),
      E.ul(
        css.Filters,
        FilterButton(todoList, Filter.All),
        FilterButton(todoList, Filter.Active),
        FilterButton(todoList, Filter.Completed),
      ),
      E.button(
        css.ClearCompleted,
        "Clear completed",
        A.onClick { _ =>
          TodosCircuit(ClearCompleted)
        },
      ),
    )

  }

  override def modelReader(p: Unit): ModelR[Root, TodoList] = circuit.zoom(_.todoList)
}
