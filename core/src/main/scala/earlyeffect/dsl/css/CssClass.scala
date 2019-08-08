package earlyeffect.dsl.css

import java.util.UUID

import earlyeffect.Attribute
import earlyeffect.dsl.css.Styles.{DeclarationOrSelector, KeyFrames, Selector}

import scala.scalajs.js

case class CssClass(id: String, members: DeclarationOrSelector*) extends Attribute {
  val name      = "class"
  val className = s"$id-earlyeffect-${UUID.randomUUID().toString}"
  val value     = className.asInstanceOf[js.Any]
  val sel       = Selector(s".$className", members: _*)
  val doc       = org.scalajs.dom.document
  val style     = doc.createElement("style")

  def mkString: String = {
    val keyframes = js.Array[KeyFrames]()
    val mainCss   = sel.mkString(className, keyframes)
    val keyFramesCss = keyframes
      .map(k => {
        Selector(s"@keyframes $className-${k.name}", k.selectors: _*)
      })
      .mkString("\n")
    mainCss + keyFramesCss
  }
  style.setAttribute("data-style-for", className)
  style.appendChild(doc.createTextNode(AutoPrefixed(mkString)))
  doc.head.appendChild(style)
}

object CssClass {
  def apply(members: DeclarationOrSelector*) = new CssClass("no-name", members: _*)
}
