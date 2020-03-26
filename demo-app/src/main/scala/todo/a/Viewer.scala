package todo.a

import earlyeffect._
import earlyeffect.dsl.css.CssClass
import todo.model.TodoList.actions.{Delete, Update}
import todo.model.{ModelCircuit, Todo}

object Viewer extends Component[Todo] with ClassSelector {

  object styles {
    import S._

    object Main
        extends CssClass(
          Selector(":hover button")(display.block)
        )

    object Toggle
        extends CssClass(
          textAlign.center,
          width.px(40),
          height.auto,
          position.absolute,
          top.zero,
          bottom.zero,
          margin("auto 0"),
          border("none"),
          appearance.none,
          opacity(0)
        )

    object ToggleLabel extends CssClass
    (
      backgroundRepeat("no-repeat"),
      backgroundPosition("center left")
    )

    object Pending
        extends CssClass(
          backgroundImage(
            "url('data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23ededed%22%20stroke-width%3D%223%22/%3E%3C/svg%3E')"
          )
        )

    object Complete
        extends CssClass(
          textDecoration("line-through"),
          color("#d9d9d9"),
          backgroundImage(
            "url('data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23bddad5%22%20stroke-width%3D%223%22/%3E%3Cpath%20fill%3D%22%235dc2af%22%20d%3D%22M72%2025L42%2071%2027%2056l-4%204%2020%2020%2034-52z%22/%3E%3C/svg%3E')"
          )
        )

    object Destroy
        extends CssClass(
          display.none,
          position.absolute,
          top.zero,
          right.px(10),
          bottom.zero,
          width.px(40),
          height.px(40),
          margin("auto 0"),
          fontSize.px(30),
          color("#cc9a9a"),
          Selector(":after")(content("'×'")),
          Selector(":hover:after")(color("#af5b5e"), transition("color 0.4s"), fontWeight(600))
        )
  }

  override def render(todo: Todo): VNode =
    E.div(
      styles.Main,
      A.`class`("view"),
      E.input(
        styles.Toggle,
        A.`type`("checkbox"),
        A.checked(todo.complete),
        A.onChange(_ => ModelCircuit(Update(todo.copy(complete = !todo.complete))))
      ),
      E.label(
        styles.ToggleLabel,
        if (todo.complete) styles.Complete else styles.Pending,
        todo.description,
        A.onDoubleClick(_ => {
          ModelCircuit.zoom(_.todoList.todos).value.foreach(x => ModelCircuit(Update(x.copy(editing = false))))
          ModelCircuit(Update(todo.copy(editing = true)))
        })
      ),
      E.button(
        styles.Destroy,
        A.`class`("destroy"),
        A.onClick(_ => ModelCircuit(Delete(todo)))
      )
    )
}
