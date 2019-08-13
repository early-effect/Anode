import earlyeffect.dsl.{css, _}
import earlyeffect.dsl.css.Styles
import earlyeffect.impl.{EarlyEffect, VNodeJS}
import earlyeffect.impl.Preact.Fragment
import org.scalajs.dom

import scala.language.implicitConversions
import scala.scalajs.js

package object earlyeffect {
  import scala.scalajs.js.JSConverters._
  private[earlyeffect] val constructors = js.Dictionary[js.Dynamic]()

  val Preact: impl.Preact.type = impl.Preact

  type ComponentFunction[T] = T => VNode

  type A = Attributes.type
  val A: A = Attributes

  type E = Elements.type
  val E: E = Elements

  type S = Styles.type
  val S: S = css.Styles

  dom.Event
  def log(m: js.Any, a: Any*): Unit = dom.window.console.log(m, a.map(_.asInstanceOf[js.Any]): _*)

  def fragment(children: Child*): VNode = EarlyEffect.h(Fragment, null, children.toJSArray)

  def when(p: => Boolean) = When(p)

  def text(s: String) = StringArg(s)

  implicit class richDouble(d: Double) {
    def pct = s"$d%"
    def px  = s"${d}px"
    def em  = s"${d}em"
  }
  implicit class richInt(n: Int) {
    def pct = s"$n%"
    def px  = s"${n}px"
    def em  = s"${n}em"
  }
  def args(as: Arg*) = Args(as)

  object preact {
    def render(node: VNode, parent: dom.Element): Unit = Preact.render(node.vnode, parent)

    def render(node: VNode, parent: dom.Element, replaceNode: dom.Element): Unit =
      Preact.render(node.vnode, parent, replaceNode)

    def rerender(): Unit = Preact.rerender()
  }

  object dictionaryNames {
    val Props                = "_early_effect_props"
    val State                = "_early_effect_state"
    val ComponentConstructor = "_early_effect_component"
  }

}
