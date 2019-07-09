package earlyeffect.dsl

import java.util.UUID

import earlyeffect.Attribute
import earlyeffect.dsl.Styles.{DeclarationOrSelector, Selector}

import scala.scalajs.js

case class CssClass(id: String, members: DeclarationOrSelector*) extends Attribute {
  val name      = "class"
  val className = s"$id-preactor-${UUID.randomUUID().toString}"
  val value     = className.asInstanceOf[js.Any]
  val sel       = Selector(s".$className", members: _*)
  val doc       = org.scalajs.dom.document
  val style     = doc.createElement("style")
  style.setAttribute("data-style-for", className)
  style.appendChild(doc.createTextNode(AutoPrefixed(sel.mkString)))
  doc.head.appendChild(style)
}
object CssClass {}
