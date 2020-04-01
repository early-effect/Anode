package todo.a

import diode.ModelR
import earlyeffect.dsl.css.CssClass
import earlyeffect._
import todo.model.{Root, TodoList}

object App extends TodoComponent[Unit, TodoList] with ClassSelector {
  override def modelReader(p: Unit): ModelR[Root, TodoList] = zoom(_.todoList)

  object Foo
      extends CssClass(
        S.color("red")
      )
  override def render(props: Unit, l: TodoList): VNode =
    E.div(
      Foo,
      E.section(
        A.`class`("todoapp"),
        E.div(
          E.header(
            A.`class`("header"),
            E.h1(
              "todos",
              A.`class`("title"),
//              A.onClick { _ =>dom.window.location.pathname = "/b"},
              E.h4("Offline").when(!l.online)
            ),
            Creator
          ),
          when(l.todos.nonEmpty) {
            fragment(
              List,
              Footer
            )
          }
        )
      ),
      E.footer(
        A.`class`("info"),
        fragment(
          E.p("Double-click to edit a todo"),
          E.p("Rendered by EarlyEffect - a Scala.js 1.0.0-RC2 wrapper for Preact")
        )
      )
    )
}
