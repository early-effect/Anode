package earlyeffect.dsl.css

import earlyeffect.dsl.css.Styles.{DeclarationOrSelector, KeyFrames, MediaQuery, Selector}
import earlyeffect.{Attribute, ClassSelector}
import org.scalajs.dom.html.Document
import org.scalajs.dom.raw.Element

import scala.scalajs.js

abstract class CssClass(ds: DeclarationOrSelector*) extends Attribute { self =>
  override val name                       = "class"
  lazy val className: String              = ClassSelector.makeCssClass(self.getClass.getName)
  def members: Seq[DeclarationOrSelector] = ds
  lazy val selector                       = s".$className"
  override val value                      = className.asInstanceOf[js.Any]
  lazy val sel: Selector                  = Selector(selector, members: _*)
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

  private def appendStyle: Unit = {
    style.setAttribute("data-style-for", className)
    style.appendChild(doc.createTextNode(AutoPrefixed(mkString)))
    doc.head.appendChild(style)
  }
  self.appendStyle
}
