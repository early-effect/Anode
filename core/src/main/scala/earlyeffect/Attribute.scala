package earlyeffect

import earlyeffect.impl.Predicated

import scala.scalajs.js

trait Attribute {
  def name: String
  def value: js.Any
}
case class SimpleAttribute(name: String, value: js.Any) extends Attribute

object Attribute {
  def apply(name: String, value: js.Any): Attribute = SimpleAttribute(name, value)
  implicit class When(a: Attribute) extends Predicated(a)
}
