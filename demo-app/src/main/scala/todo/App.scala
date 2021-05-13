package todo

import anode.dsl.Elements.div
import anode.dsl.css.CssClass
import anode.{A, Args, ClassSelector, E, S, VNode, args, fragment, log, when}
import diode.ModelR
import org.scalajs.dom
import todo.model.{Root, TodoList}



object App extends TodoComponent[Unit, TodoList] with ClassSelector {

  object css {
    import S._

    object App
        extends CssClass(
          font("14px 'Helvetica Neue', Helvetica, Arial, sans-serif"),
          lineHeight.em(1.4),
          background("#e5e5e5"),
          color("#4d4d4d"),
          minWidth.px(230),
          maxWidth.px(550),
          margin.apply("0", "auto"),
          fontWeight(300),
          textRendering.optimizeLegibility,
          S("-webkit-font-smoothing", "antialiased"),
        )

    object Info
        extends CssClass(
          margin("65px auto 0"),
          color.rgba(0, 0, 0, .6),
          fontSize.px(12),
          fontWeight(400),
          S("-webkit-font-smoothing", "antialiased"),
          textRendering.optimizeLegibility,
          textAlign.center,
          Selector(" p", S.lineHeight("1")),
        )

    object TodoApp
        extends CssClass(
          background("#ffffff"),
          margin("130px", "0", "40px", "0"),
          position.relative,
          boxShadow("0 3px 3px 0 rgba(0, 0, 0, 0.4), 0 25px 50px 0 rgba(0, 0, 0, 0.2)"),
          Selector(" :focus", outline("0")),
        )

    object Header
        extends CssClass(
          position.absolute,
          top.px(-155),
          width.pct(100),
          fontSize.px(100),
          fontWeight(300),
          textAlign.center,
          color.rgba(175, 47, 47, 0.35),
          textShadow("3px 3px 5px rgba(0,0,0,0.4)"),
        )
  }

  override def modelReader(p: Unit): ModelR[Root, TodoList] = zoom(_.todoList)
  import anode.Formable
  import Formable.defaultImplicits._
  sealed trait Foo{
    def a:String
  }
  case class Bar(a:String,b:Map[String, String]) extends Foo
  implicit val showMap:Formable[Map[String,String]] = Formable(formProps => {
    implicit val tuple:Formable[(String,String)] = Formable(tupleProps =>{
      args(E.div(tupleProps.field._1), E.input(tupleProps.field._2, A.onKeyUp(x => {
        tupleProps.update(tupleProps.field._1, x.target.asInstanceOf[dom.html.Input].value)
      })))
    })
    args(formProps.field.toSeq.map[Args](x => Formable("",x)(x => formProps.update(formProps.field + x))))
  })
  val f = Bar("Russ",Map("Foo" -> "Bar", "baz" -> "bonk"))
  override def render(props: Unit, l: TodoList): VNode =
    E.body(
      Formable("",f)(f => log("f",f)),
      css.App,
      E.section(
        css.TodoApp,
        E.div(
          E.header(
            E.h1(
              css.Header,
              "todos",
            ),
            Creator,
          ),
          when(l.todos.nonEmpty) {
            fragment(
              List,
              Footer,
            )
          },
        ),
      ),
      E.footer(
        css.Info,
        fragment(
          E.p("Double-click to edit a todo"),
          E.p("Rendered by ", E.strong("Anode"), " - a Scala.js wrapper for Preact"),
        ),
      ),
    )
}
