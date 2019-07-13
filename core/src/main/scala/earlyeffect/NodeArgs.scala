package earlyeffect

import earlyeffect.impl.Preact.{AttributeOrChild, Child}

import scala.scalajs.js

case class NodeArgs(args: Seq[AttributeOrChild]) {
  lazy val attributes: js.Array[Attribute] = {
    val as = js.Array[Attribute]()
    args.foreach { a =>
      val m = a.merge[Any]
      m match {
        case x: Attribute   => as.push(x)
        case args: NodeArgs => as.push(args.attributes: _*)
        case _              =>
      }
    }
    as
  }
  lazy val children: js.Array[Child] = {
    val cs = js.Array[Child]()
    args.foreach { a =>
      val m = a.merge[Any]
      m match {
        case as: NodeArgs => cs.push(as.children: _*)
        case _: Attribute =>
        case _            => cs.push(a.asInstanceOf[Child])
      }
    }
    cs
  }
}
