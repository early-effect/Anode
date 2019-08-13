package earlyeffect

import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.UndefOr

abstract class Component[Props] extends EarlyComponent[Props, Nothing] { self =>

  def render(props: Props): VNode

  def didUpdate(oldProps: Props, instance: Instance, oldInstance: UndefOr[Instance]): Unit = ()

  def shouldUpdate(nextProps: Props, previous: Instance): Boolean =
    previous.props != nextProps

  override val instanceConstructor: js.Dynamic =
    constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[Component.Instance[Props]])

}

object Component {

  class Instance[Props] extends InstanceFacade[Props, Component[Props], Nothing] {
    override def render(p: js.Dynamic, s: js.Dynamic): VNodeJS = {
      val comp = lookupComponent(p)
      comp.render(props).vnode
    }

    override def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      lookupComponent().didUpdate(
        lookupProps(oldProps),
        this.instance,
        snapshot.asInstanceOf[UndefOr[Instance[Props]]].map(_.instance)
      )

    override def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      lookupComponent().shouldUpdate(lookupProps(nextProps), this.instance)
  }

  implicit def applySelf[Comp <: Component[Comp], T <: Arg](self: Comp): T = self.apply(self).asInstanceOf[T]
}
