package anode

import org.scalajs.dom

trait InstanceDataSelector {comp:AnodeComponent[_,_] =>
  val attributeName = s"data-anode-$classForClass"
  def extractAttributeValue(instance: comp.Instance): String
  def selector(attributeValue: String) = s"[$attributeName='$attributeValue']"

  def addDataAttribute(e: dom.Element, instance: Instance): Unit =
    e.setAttribute(attributeName, comp.extractAttributeValue(instance))
}
