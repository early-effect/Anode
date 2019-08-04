package earlyeffect

import earlyeffect.impl.EarlyEffect

import scala.scalajs.js
import scala.scalajs.js.Dictionary

trait EarlyComponent[Props, State] { self =>
  import dictionaryNames._

  def instanceConstructor: js.Dynamic

  val defaultKey = self.getClass.getName

  type I = EarlyInstance[Props, State]

  def didMount(instance: I): Unit    = ()
  def willMount(instance: I): Unit   = ()
  def willUnMount(instance: I): Unit = ()

  def didUpdate[T <: I](oldProps: Props, oldState: State, instance: T): Unit = ()

  def baseDictionary(args: (String, js.Any)*): Dictionary[js.Any] =
    js.Dictionary(
      args ++ Seq[(String, js.Any)]((ComponentConstructor, self.asInstanceOf[js.Any]), ("key", defaultKey)): _*
    )

  def apply(props: Props): VNode =
    VNode(EarlyEffect.h(instanceConstructor, baseDictionary(Props -> props.asInstanceOf[js.Any])))

}
