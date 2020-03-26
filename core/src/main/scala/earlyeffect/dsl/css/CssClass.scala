package earlyeffect.dsl.css

import java.util.UUID

import earlyeffect.Attribute
import earlyeffect.dsl.css.Styles.{DeclarationOrSelector, KeyFrames, MediaQuery, Selector}
import org.scalajs.dom.html.Document
import org.scalajs.dom.raw.Element

import scala.scalajs.js

abstract class CssClass(ds: DeclarationOrSelector*) extends Attribute { self =>
  override val name                       = "class"
  val className: String                   = self.getClass.getName.replaceAll("[^\\w]", "_")
  def members: Seq[DeclarationOrSelector] = ds
  val selector                            = s".$className"
  override val value                      = className.asInstanceOf[js.Any]
  val sel: Selector                       = Selector(selector, members: _*)
  private def doc: Document               = org.scalajs.dom.document
  private lazy val style: Element         = doc.createElement("style")

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

case class Css(prefix: String) {

  object Css {
    def apply(prefix: String): Css = new Css(prefix)
  }

  @deprecated("Use: CssClass(...) instead...", "0.3.3")
  case class Class(val id: String, override val members: Seq[DeclarationOrSelector]) extends CssClass {
    override val className = s"${prefix}__$id"
  }

  object Class {
    def apply(members: DeclarationOrSelector*) = new Class(UUID.randomUUID().toString, members)
  }

}
