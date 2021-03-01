package anode

import org.scalajs.dom

trait ClassSelector { comp:AnodeComponent[_,_] =>
  def selector = s".${comp.classForClass}"

  def addClass(e: dom.Element): Unit = if(!e.classList.contains(classForClass)) e.classList.add(classForClass)

}

object ClassSelector {

  def makeCssClass(className: String): String = {
    val res = className.replaceAll("[^\\w]", "-")
    if (res.endsWith("-")) res.dropRight(1) else res
  }
}
