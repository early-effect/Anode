package earlyeffect

import earlyeffect.dsl.css.Styles.{DeclarationOrSelector, KeyFrames, MediaQuery}
import earlyeffect.impl.Preact.ChildJS
import earlyeffect.impl.VNodeJS
import org.scalajs.dom

import scala.language.implicitConversions
import scala.scalajs.js

sealed trait Arg { self =>
  def when(pred: => Boolean) = if (pred) self else Empty
}

case object Empty extends Arg

trait Child extends Arg { self =>
  def value: Preact.ChildJS
  override def when(pred: => Boolean): Child = if (pred) self else EmptyChild
}

trait VNode extends Child {

  def withT[T <: js.Any](name: String, t: T): VNode = {
    val props = vnode.props.asInstanceOf[js.Dictionary[js.Any]]
    vnode.ref.foreach(props.update("ref", _))
    vnode.key.foreach(props.update("key", _))
    props.update(name, t)
    Preact.h(
      vnode.`type`.asInstanceOf[js.Dynamic],
      props,
      vnode.rawChildren
    )
  }

  def children: js.Array[VNode] =
    vnode.childArray.map(x => x: VNode)

  def withKey(key: String): VNode = withT(name = "key", key.asInstanceOf[js.Any])

  def withRef(f: js.Function1[dom.Element, Unit]): VNode = {

    val combined = vnode.ref.fold(f)(
      existing =>
        (e: dom.Element) => {
          existing(e)
          f(e)
        }
    )

    val safe: js.Function1[js.Any, Unit] = {
      case null                    => ()
      case e: dom.Element          => combined(e)
      case i: InstanceFacade[_, _] => i.base.foreach(combined)
      case x: js.Any =>
        Option(x.asInstanceOf[js.Dynamic].base).map(_.asInstanceOf[dom.Element]).foreach(combined)
    }
    withT(name = "ref", safe)
  }

  def value: ChildJS = vnode
  def vnode: VNodeJS
}

object VNode {
  implicit def toJS(c: VNode): VNodeJS = c.vnode
}

object Child {
  implicit def fromOption(o: Option[Child]): Child = o.getOrElse(EmptyChild)
  implicit def toJS(c: Child): Preact.ChildJS      = c.value
}

case object EmptyChild extends Child {
  override def value: ChildJS = null
}

trait Attribute extends Arg {
  def name: String
  def value: js.Any

  override def toString: String = s"$name: ${value.toString}"
}

final case class SimpleAttribute(name: String, value: js.Any) extends Attribute

object Attribute {
  def apply(name: String, value: js.Any): Attribute = SimpleAttribute(name, value)
}

final case class Declaration(property: String, value: String) extends Arg with DeclarationOrSelector {
  override def mkString(className: String, kf: js.Array[KeyFrames], mq: js.Array[MediaQuery]): String =
    s"$property: $value;"

  def important: Declaration = copy(value = s"$value !important")
}

object Declaration {
//  def apply[T](property: String): DeclarationConstructor[T] = SimpleConstructor[T](property)
}

final case class StringArg(s: String) extends Child {
  override def value: Preact.ChildJS = s
}

final case class DoubleArg(d: Double) extends Child {
  override def value: Preact.ChildJS = d.toString
}

object Arg {
  implicit def stringToArg(s: String): Arg                = StringArg(s)
  implicit def doubleToArg(d: Double): Arg                = DoubleArg(d)
  implicit def intToArg(i: Int): Arg                      = DoubleArg(i)
  implicit def fromOption(o: Option[Arg]): Arg            = o.getOrElse(Empty)
  implicit def seqToArgs(s: Seq[Arg]): Arg                = Args(s)
  implicit def arrayToArgs[A <: Arg](s: js.Array[A]): Arg = Args(s)
}

case class Args(args: Seq[Arg]) extends Arg {

  lazy val attributeDictionary =
    js.Dictionary(normalizeStyles(normalizeClasses(attributes)).map(x => x.name -> x.value).toSeq: _*)

  def normalizeClasses(attrs: js.Array[Attribute]): js.Array[Attribute] = {
    val classes = attrs.filter(_.name == "class")
    if (classes.length <= 1) attrs
    else {
      val combinedClass = A.`class`(classes.map(x => x.value).mkString(" "))
      attrs.filterNot(x => classes.contains(x)) :+ combinedClass
    }
  }

  def normalizeStyles(attrs: js.Array[Attribute]): js.Array[Attribute] = {
    val styles = attributes.filter(_.name == "style")
    if (styles.length <= 1) attrs
    else attrs.filterNot(x => styles.contains(x)) :+ combineStyles(styles.toSeq: _*)
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
    if (args != null) {
      args.foreach {
        case a: Attribute   => as.push(a)
        case na: Args       => as.push(na.attributes.toSeq: _*)
        case d: Declaration => ds.push(d)
        case _              =>
      }
      if (ds.nonEmpty) as.push(A.style(ds.toSeq: _*))
    }
    as
  }
  lazy val children: js.Array[Child] = {
    val cs = js.Array[Child]()
    if (args != null) {
      args.foreach {
        case c: Child => cs.push(c)
        case na: Args => cs.push(na.children.toSeq: _*)
        case null     => cs.push(EmptyChild)
        case _        =>
      }
    }
    cs
  }
}

object Args {
  def apply[A <: Arg](args: js.Array[A]) = new Args(args.toSeq)
}
