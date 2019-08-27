package earlyeffect.dsl.css

import java.util.UUID

import earlyeffect.Attribute
import earlyeffect.dsl.css.Styles.{DeclarationOrSelector, KeyFrames, MediaQuery, Selector}

import scala.scalajs.js

case class Css(prefix: String) {
  case class Class(id: String, members: DeclarationOrSelector*) extends Attribute {
    val name      = "class"
    val className = s"${prefix}__$id"
    val selector  = s".$className"
    val value     = className.asInstanceOf[js.Any]
    val sel       = Selector(s".$className", members: _*)
    val doc       = org.scalajs.dom.document
    val style     = doc.createElement("style")

    def mkString: String = {
      val keyframes    = js.Array[KeyFrames]()
      val mediaQueries = js.Array[MediaQuery]()
      val mainCss      = sel.mkString(className, keyframes, mediaQueries)
      val keyFramesCss = keyframes
        .map(k => {
          Selector(s"@keyframes $className-${k.name}", k.selectors: _*)
        })
        .mkString("\n")
      val mediaQueriesCss = mediaQueries.map(_.render).mkString("\n")
      mainCss + keyFramesCss + mediaQueriesCss
    }
    style.setAttribute("data-style-for", className)
    style.appendChild(doc.createTextNode(AutoPrefixed(mkString)))
    doc.head.appendChild(style)
  }

  object Class {
    def apply(members: DeclarationOrSelector*) = new Class(UUID.randomUUID().toString, members: _*)
  }

}