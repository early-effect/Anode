package earlyeffect

import earlyeffect.impl.EarlyEffect

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.{Dictionary, UndefOr}

trait ClassSelector { self: EarlyComponent[_, _] =>
  def selector = s".$defaultKey"

  def addClass(instance: Instance): Unit =
    instance.base.foreach { e =>
      e.setAttribute(
        name = "class",
        value = Option(e.getAttribute("class")).fold(defaultKey)(x => x + " " + defaultKey)
      )
    }

}

trait InstanceDataSelector { self: EarlyComponent[_, _] =>
  val attributeName = s"data-earlyeffect-$defaultKey"
  def extractAttributeValue(s: self.Instance): String
  def selector(attributeValue: String) = s"[$attributeName='$attributeValue']"

  def addDataAttribute(instance: self.Instance): Unit =
    instance.base.foreach { e =>
      e.setAttribute(attributeName, self.extractAttributeValue(instance))
    }
}

trait EarlyComponent[Props, State] { self =>

  type P = Props

  import dictionaryNames._

  def instanceConstructor: js.Dynamic

  val defaultKey = self.getClass.getName.replaceAll("[^\\w]", "_")

  type Instance = EarlyInstance[Props, State]

  def didMount(instance: Instance): Unit = ()

  def willMount(instance: Instance): Unit   = ()
  def willUnMount(instance: Instance): Unit = ()

  def didUpdate(oldProps: Props, oldState: State, instance: Instance, oldInstance: UndefOr[Instance]): Unit = ()

  def baseDictionary(props: Props): Dictionary[js.Any] =
    js.Dictionary(
      Seq[(String, js.Any)](
        (Props, props.asInstanceOf[js.Any]),
        (ComponentConstructor, self.asInstanceOf[js.Any]),
        ("key", defaultKey) // this is a precaution - I may want to make this optional
      ): _*
    )

  def apply(props: Props): VNode =
    EarlyEffect.h(instanceConstructor, baseDictionary(props))

}

object EarlyComponent {
  implicit def toVNode(ec: EarlyComponent[Unit, _]): VNode = ec.apply(Unit)
}
