package earlyeffect

import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js

abstract class Component[Props] extends ComponentOps[Props, Nothing] { self =>

  def render(props: Props): VNode

  def didUpdate(oldProps: Props, instance: I): Unit = ()

  def shouldUpdate(nextProps: Props, previous: I): Boolean =
    previous.props != nextProps

  override val instanceConstructor: js.Dynamic =
    constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[Component.Instance[Props]])

}

object Component {

  class Instance[Props] extends BaseInstance[Props, Component[Props], Nothing] {
    override def render(p: js.Dynamic, s: js.Dynamic): VNodeJS = lookupComponent(p).render(lookupProps(p)).vn

    //TODO: not sure what to do with snapshot...
    def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      lookupComponent().didUpdate(lookupProps(oldProps), this.instance)

    def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      lookupComponent().shouldUpdate(lookupProps(nextProps), this.instance)
  }

  implicit def applySelf[Comp <: Component[Comp], T <: Arg](self: Comp): T = self.apply(self).asInstanceOf[T]
}
