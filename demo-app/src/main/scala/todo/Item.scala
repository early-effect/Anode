package todo

import anode.dsl.css.CssClass
import anode.{A, Component, E, S, VNode}
import todo.model.Todo

object Item extends Component[Todo] {

  object styles {
    import S._

    object Base
        extends CssClass(
          position.relative,
          fontSize.px(24),
          borderBottom("1px solid #ededed"),
          Selector(" label")(
            S("word-break")("break-all"),
            padding("15px 15px 15px 60px"),
            display.block,
            S.backgroundRepeat("no-repeat"),
            lineHeight.em(1.2),
            transition("color .4s"),
            fontSize.px(24),
          ),
          Selector(":last-child")(
            borderBottom("none")
          ),
        )

    object Editing
        extends CssClass(
          S.borderBottom("none"),
          S.padding.zero,
        )
  }

  override def render(todo: Todo): VNode =
    E.li(
      styles.Base,
      styles.Editing.when(todo.editing),
      if (todo.editing) {
        Editor(todo)
      } else {
        Viewer(todo)
      },
    )
}
