package todo.a

import earlyeffect.{Component, VNode, _}
import todo._
import todo.model.Todo

object Item extends Component[Todo] {

  object styles {
    import S._

    val Base = css("item-li")(
      Selector(" label")(
        S("word-break")("break-all"),
        padding("15px 15px 15px 60px"),
        display.block,
        lineHeight.em(1.2),
        transition("color .4s")
      ),
      Selector(":last-child")(
        borderBottom("none")
      )
    )
  }

  val Editing = css("editing")(
    S.selector(":last-child")(
      S.marginBottom.px(-1)
    )
  )
  override def render(todo: Todo): VNode =
    E.li(
      styles.Base,
      A.`class`(if (todo.editing) "editing" else "view"),
      if (todo.editing) {
        Editor(todo)
      } else {
        Viewer(todo)
      }
    )
}
