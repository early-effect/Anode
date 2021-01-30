package earlyeffect

import org.scalajs.dom

trait ClassSelector { self: EarlyComponent[_, _] =>
  def selector = s".$classForClass"

  def addClass(e: dom.Element): Unit = {
    val newClass = Option(e.getAttribute("class")).fold(classForClass) { old =>
      if (old.endsWith(classForClass)) old else old + " " + classForClass
    }
    e.setAttribute(
      name = "class",
      value = newClass,
    )
  }

}

object ClassSelector {

  def makeCssClass(className: String): String = {
    val res = className.replaceAll("[^\\w]", "-")
    if (res.endsWith("-")) res.dropRight(1) else res
  }
}
