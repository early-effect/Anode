package earlyeffect

import earlyeffect.impl.EarlyEffect
import org.scalajs.dom.Element

import scala.scalajs.js
import scala.scalajs.js.{Dictionary, UndefOr}

trait EarlyComponent[Props, State] { self =>
  import dictionaryNames._

  def instanceConstructor: js.Dynamic

  val defaultKey = self.getClass.getName

  def selector = s"[data-component='$defaultKey']"

  type Instance = EarlyInstance[Props, State]

  def didMount(instance: Instance): Unit    = ()
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
    VNode(EarlyEffect.h(instanceConstructor, baseDictionary(props)))

  val addDataComponentAttribute: js.Function1[Element, Unit] = e => e.setAttribute("data-component", defaultKey)

  def addDataComponent(res: VNode): VNode =
    res.vn.ref
      .fold(res.withRef(addDataComponentAttribute))(
        wr =>
          res.withRef(e => {
            wr(e)
            addDataComponentAttribute(e)
          })
      )

}
