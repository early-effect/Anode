package earlyeffect

import earlyeffect.impl.EarlyEffect
import org.scalajs.dom

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.{Dictionary, UndefOr}

//noinspection ScalaUnusedSymbol
trait EarlyComponent[Props, State] { self =>

  type P = Props

  import dictionaryNames._

  def instanceConstructor: js.Dynamic

  lazy val classForClass = ClassSelector.makeCssClass(self.getClass.getName)

  type Instance = EarlyInstance[Props, State]

  def didMount(instance: Instance): Unit = ()

  def willMount(instance: Instance): Unit = ()

  def willUnMount(instance: Instance): Unit = ()

  def didCatch(e: js.Error, instance: Instance): Unit = ()

  def didUpdate(
      oldProps: Props,
      oldState: State,
      instance: Instance,
      oldInstance: UndefOr[Instance],
  ): Unit = ()

  def baseDictionary(props: Props): Dictionary[js.Any] =
    js.Dictionary(
      Seq[(String, js.Any)](
        (PropsFieldName, props.asInstanceOf[js.Any]),
        ("key", classForClass), // this is a precaution - I may want to make this optional
      ): _*
    )

  def apply(props: Props): VNode = EarlyEffect.h(instanceConstructor, baseDictionary(props))

  def addSelectors(n: VNode, facade: InstanceFacade[Props, State]): VNode =
    self match {
      case selectors: ClassSelector with InstanceDataSelector =>
        n.withRef { e =>
          selectors.addDataAttribute(e, facade)
          selectors.addClass(e)
        }
      case classSelector: ClassSelector => n.withRef(e => classSelector.addClass(e))
      case instanceDataSelector: InstanceDataSelector =>
        n.withRef(e => instanceDataSelector.addDataAttribute(e, facade))
      case _ => n
    }

}

object EarlyComponent {
  implicit def toVNode(ec: EarlyComponent[Unit, _]): VNode = ec.apply(())
}
