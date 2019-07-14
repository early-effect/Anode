package earlyeffect

import earlyeffect.dsl.Styles.{Constructor, DeclarationConstructor, DeclarationOrSelector, KeyFrames}
import earlyeffect.impl.Preact.ChildJS
import earlyeffect.impl.{Predicated, VNodeJS}

import scala.language.implicitConversions
import scala.scalajs.js

sealed trait Arg

sealed trait Child extends Arg {
  def value: Preact.ChildJS
}

trait Attribute extends Arg {
  def name: String
  def value: Any

  override def toString: String = s"$name: ${value.toString}"
}

final case class SimpleAttribute(name: String, value: js.Any) extends Attribute

object Attribute {
  def apply(name: String, value: js.Any): Attribute = SimpleAttribute(name, value)
  implicit class When(a: Attribute) extends Predicated(a)
}

final case class Declaration(property: String, value: String) extends Arg with DeclarationOrSelector {
  override def mkString(className: String, kf: js.Array[KeyFrames]): String = s"$property: $value;"
}

object Declaration {
  def apply[T](property: String): DeclarationConstructor[T] = Constructor[T](property)

  implicit class When(p: Declaration) extends Predicated(p)
}

final case class VNode(vn: VNodeJS) extends Child {
  override def value: Preact.ChildJS = vn
}

final case class StringArg(s: String) extends Child {
  override def value: Preact.ChildJS = s
}

final case class DoubleArg(d: Double) extends Child {
  override def value: Preact.ChildJS = d.toString
}

object Arg {
  val Empty: Arg                                       = null
  implicit def stringToArg(s: String): StringArg       = StringArg(s)
  implicit def doubleToArg(d: Double): DoubleArg       = DoubleArg(d)
  implicit def toPreactChild(c: Child): Preact.ChildJS = c.value
  implicit def maybeArg(o: Option[Arg]): Arg =
    o.fold(Empty)(x => {
      x
    })
  implicit def seqToArgs(s: Seq[Arg]): NodeArgs = NodeArgs(s)
}

case class NodeArgs(args: Seq[Arg]) extends Arg {

  lazy val attributeDictionary =
    js.Dictionary(normalizeStyles(normalizeClasses(attributes)).map(x => x.name -> x.value.asInstanceOf[js.Any]): _*)

  def normalizeClasses(attrs: Seq[Attribute]): Seq[Attribute] = {
    val classes = attrs.filter(_.name == "class")
    if (classes.length <= 1) attrs
    else {
      val combinedClass = A.`class`(classes.map(x => x.value).mkString(" "))
      attrs.filterNot(x => classes.contains(x)) :+ combinedClass
    }
  }

  def normalizeStyles(attrs: Seq[Attribute]): Seq[Attribute] = {
    val styles = attributes.filter(_.name == "style")
    if (styles.length <= 1) attrs
    else attrs.filterNot(x => styles.contains(x)) :+ combineStyles(styles: _*)
  }

  def combineStyles(attrs: Attribute*): Attribute = {
    val d = js.Dictionary[String]()
    attrs.foreach(a => {
      a.value.asInstanceOf[js.Dictionary[String]].foreach(x => d.update(x._1, x._2))
    })
    Attribute("style", d)
  }

  lazy val attributes: js.Array[Attribute] = {
    val as = js.Array[Attribute]()
    val ds = js.Array[Declaration]()
    args.foreach {
      case a: Attribute   => as.push(a)
      case na: NodeArgs   => as.push(na.attributes: _*)
      case d: Declaration => ds.push(d)
      case _              =>
    }
    if (ds.nonEmpty) as.push(A.style(ds: _*))
    as
  }
  lazy val children: js.Array[Child] = {
    val cs = js.Array[Child]()
    args.foreach {
      case c: Child     => cs.push(c)
      case na: NodeArgs => cs.push(na.children: _*)
      case _            =>
    }
    cs
  }
}
