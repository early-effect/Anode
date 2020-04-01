package earlyeffect

import earlyeffect.impl.EarlyEffect
import org.scalajs.dom

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.{Dictionary, UndefOr}

trait ClassSelector { self: EarlyComponent[_, _] =>
  def selector = s".$classForClass"

  def addClass(e: dom.Element): Unit = {
    val newClass = Option(e.getAttribute("class")).fold(classForClass)(old => {
      if (old.contains(classForClass)) old else old + " " + classForClass
    })
    e.setAttribute(
      name = "class",
      value = newClass
    )
  }

}

object ClassSelector {

  def makeCssClass(className: String): String = {
    val res = className.replaceAll("[^\\w]", "-")
    if (res.endsWith("-")) res.dropRight(1) else res
  }
}

trait InstanceDataSelector { self: EarlyComponent[_, _] =>
  val attributeName = s"data-earlyeffect-$classForClass"
  def extractAttributeValue(instance: self.Instance): String
  def selector(attributeValue: String) = s"[$attributeName='$attributeValue']"

  def addDataAttribute(e: dom.Element, instance: Instance): Unit =
    e.setAttribute(attributeName, self.extractAttributeValue(instance))
}

trait EarlyComponent[Props, State] { self =>

  type P = Props

  import dictionaryNames._

  def instanceConstructor: js.Dynamic

  lazy val classForClass = ClassSelector.makeCssClass(self.getClass.getName) + "__ClassSelector"

  type Instance = EarlyInstance[Props, State]

  def didMount(instance: Instance): Unit = ()

  def willMount(instance: Instance): Unit = ()

  def willUnMount(instance: Instance): Unit = ()

  def didUpdate(
      oldProps: Props,
      oldState: State,
      instance: Instance,
      oldInstance: UndefOr[Instance]
  ): Unit = ()

  def baseDictionary(props: Props): Dictionary[js.Any] =
    js.Dictionary(
      Seq[(String, js.Any)](
        (PropsFieldName, props.asInstanceOf[js.Any]),
        ("key", classForClass) // this is a precaution - I may want to make this optional
      ): _*
    )

  def apply(props: Props): VNode = EarlyEffect.h(instanceConstructor, baseDictionary(props))

  def addSelectors(n: VNode, facade: InstanceFacade[Props, State]): VNode =
    self match {
      case classSelector: ClassSelector => n.withRef(e => classSelector.addClass(e))
      case instanceDataSelector: InstanceDataSelector =>
        n.withRef(e => instanceDataSelector.addDataAttribute(e, facade))
      case _ => n
    }

}

object EarlyComponent {
  implicit def toVNode(ec: EarlyComponent[Unit, _]): VNode = ec.apply(())
}
