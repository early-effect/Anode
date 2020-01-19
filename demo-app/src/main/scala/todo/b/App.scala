package todo.b

import todo._
import todo.model.TodoList.actions._
import todo.model._
import diode._
import earlyeffect._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement

object App {

  object styles {
    import S._

    dom.document.head.appendChild {
      val element = dom.document.createElement("style")
      val selector = Selector("body")(
        background("#f5f5f5"),
        font("font: 14px 'Helvetica Neue', Helvetica, Arial, sans-serif"),
        lineHeight.em(1.4),
        color("#4d4d4d"),
        minWidth.px(230),
        maxWidth.px(550),
        margin("0 auto"),
        fontWeight(300),
        padding.zero
      )
      element.appendChild(dom.document.createTextNode(selector.mkString("")))
      element
    }

    val App = css("app")(
      background("white"),
      marginTop.px(130),
      marginBottom.px(40),
      marginLeft.zero,
      marginRight.zero,
      position.relative,
      boxShadow("0 2px 4px 0 rgba(0,0,0,0.2), 0 25px 50px 0 rgba(0,0,0,0.1)")
    )

    val Title = css("title")(
      position.absolute,
      top.px(-155),
      width.pct(100),
      fontSize.px(100),
      fontWeight(100),
      textAlign.center,
      color.rgba(175, 47, 47, 0.15),
      textRendering.optimizeLegibility,
      textAlign.center
    )

    val Input = css("todo-input")(
      position.relative,
      margin.zero,
      width.pct(100),
      fontSize.px(24),
      fontFamily.inherit,
      fontWeight.inherit,
      lineHeight.em(1.4),
      paddingBottom.px(16),
      paddingTop.px(16),
      paddingLeft.px(16),
      paddingRight.px(16),
      border("none"),
      color.inherit,
      Selector(":focus")(outline("0")),
      Selector("::placeholder")(
        fontStyle.italic,
        fontWeight(300),
        color("#e6e6e6")
      )
    )

    val NewTodo = css("new-todo")(
      background("rgba(0, 0, 0, 0.0003)"),
      boxShadow("nset 0 -2px 1px rgba(0,0,0,0.03);")
    )

    val EditTodo = css("edit-todo")(
      )
  }

  abstract class TodoComponent[P, S] extends CircuitComponent[P, Root, S] {
    override def circuit: Circuit[Root] = ModelCircuit
  }

  object TodoListApp extends TodoComponent[Unit, TodoList] with ClassSelector {
    override def modelReader(p: Unit): ModelR[Root, TodoList] = zoom(_.todoList)

    override def render(props: Unit, l: TodoList): VNode =
      E.div(
        E.section(
          styles.App,
          E.div(
            E.header(
              S.color.inherit,
              E.h1("todos - B", A.onClick { _ =>
                dom.window.location.pathname = "/"
              }, styles.Title),
              newTodo
            ),
            when(l.todos.nonEmpty) {
              fragment(
                ListOfTodos,
                footer(l)
              )
            }
          )
        ),
        E.footer(
          A.`class`("info"),
          fragment(
            E.p("Double-click to edit a todo"),
            E.p("Rendered by EarlyEffect - a Scala.js wrapper for Preact")
          )
        )
      )
  }

  object ListOfTodos extends TodoComponent[Unit, Seq[Todo]] with ClassSelector {

    object styles {
      import S._

      val TodoList = css("todo-list")(
        margin.zero,
        padding.zero,
        listStyle.none
      )

      val Label = css("toggle-all-label")(
        Selector(":before")(
          content("\">\""),
          fontSize.px(22),
          color("#e6e6e6"),
          padding("10px 27px 10px 27px")
        )
      )

      val ToggleAll = css("toggle-all")(
        opacity(0),
        width.px(60),
        height.px(34),
        fontSize.zero,
        position.absolute,
        top.px(-52),
        left.px(-13),
        transform("rotate(90deg)")
      )
    }

    override def modelReader(p: Unit): ModelR[Root, Seq[Todo]] = zoom(_.todoList.filtered)

    override def render(props: Unit, todos: Seq[Todo]): VNode =
      E.section(
        S.position.relative,
        S.zIndex(2),
        S.borderTop("1px solid #e6e6e6"),
        E.input(
          styles.ToggleAll,
          A.id("toggle-all"),
          A.`type`("checkbox"),
          A.checked(todos.forall(_.complete)),
          A.onChange(e => {
            ModelCircuit(SetAll(e.target.asInstanceOf[HTMLInputElement].checked))
          })
        ),
        E.label(
          A.`for`("toggle-all"),
          styles.ToggleAll,
          styles.Label
        ),
        E.ul(
          styles.TodoList,
          todos.map(x => Item(x).withKey(x.key))
        )
      )
  }

  val newTodo = E.input(
    A.id("new-todo"),
    A.placeholder("What needs to be done?"),
    styles.Input,
    styles.NewTodo,
    A.onKeyDown(x => {
      val e = x.target.asInstanceOf[HTMLInputElement]
      if (x.keyCode == ENTER && e.value.nonEmpty) {
        ModelCircuit(Add(Todo(e.value)))
        e.value = ""
      }
    })
  )

  def footer(todoList: TodoList) = {
    val left = todoList.todos.count(!_.complete)
    val s    = if (left > 1) " items left" else " item left"

    def change(f: Filter) = ModelCircuit(ApplyFilter(f))

    E.footer(
      A.`class`("footer"),
      E.span(A.`class`("todo-count"), E.strong(left), E.span(s)),
      E.ul(
        A.`class`("filters"),
        E.li("All", A.`class`("selected").when(todoList.filter == All), A.onClick(_ => change(All))),
        E.li("Active", A.`class`("selected").when(todoList.filter == Active), A.onClick(_ => change(Active))),
        E.li("Completed", A.`class`("selected").when(todoList.filter == Completed), A.onClick(_ => change(Completed)))
      ),
      E.button(A.`class`("clear-completed"), "Clear completed", A.onClick(_ => {
        ModelCircuit(ClearCompleted)
      }))
    )
  }

  object Item extends Component[Todo] with ClassSelector {

    val Style = css("item")(
      S.position.relative,
      S.fontSize.px(24),
      S.borderBottom("1px solid #ededed")
    )
    override def render(todo: Todo): VNode =
      E.li(
        Style,
        A.key(todo.key),
        A.`class`("editing").when(todo.editing),
        if (todo.editing) {
          TodoEditor(todo)
        } else {
          View(todo)
        }
      )
  }

  object View extends Component[Todo] with ClassSelector {

    val Toggle = css("toggle")(
      S.textAlign.center,
      S.width.px(40),
      S.height.auto,
      S.position.absolute,
      S.top.zero,
      S.bottom.zero,
      S.margin("auto 0"),
      S.border("none"),
      S.appearance.none
    )
    override def render(todo: Todo): VNode =
      E.div(
        E.input(
          Toggle,
          A.`type`("checkbox"),
          A.checked(todo.complete),
          A.onChange(_ => ModelCircuit(Update(todo.copy(complete = !todo.complete))))
        ),
        E.label(
          todo.description,
          A.onDoubleClick(_ => {
            ModelCircuit.zoom(_.todoList.todos).value.foreach(x => ModelCircuit(Update(x.copy(editing = false))))
            ModelCircuit(Update(todo.copy(editing = true)))
          })
        ),
        E.button(A.`class`("destroy"), A.onClick(_ => ModelCircuit(Delete(todo))))
      )
  }

}
