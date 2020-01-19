package earlyeffect

import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSName

trait Component[Props] extends EarlyComponent[Props, Nothing] { theComponent =>

  def render(props: Props): VNode

  def didUpdate(oldProps: Props, instance: I, oldInstance: UndefOr[I]): Unit = ()

  def shouldUpdate(nextProps: Props, previous: I): Boolean =
    previous.props != nextProps

  override lazy val instanceConstructor: js.Dynamic = js.constructorOf[Instance]

  final private class Instance extends InstanceFacade[Props, Nothing] {

    override def componentDidMount(): Unit = didMount(this)

    override def componentWillMount(): Unit = willMount(this)

    override def componentWillUnmount(): Unit = willUnMount(this)

    @JSName("render")
    override def renderJS(props: js.Dynamic, state: js.Dynamic): VNodeJS =
      addSelectors(render(lookupProps(props)), theComponent)

    override def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, nextContext: js.Dynamic): Boolean =
      theComponent.shouldUpdate(lookupProps(nextProps), this)

    override def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      didUpdate(
        lookupProps(oldProps),
        lookupState(oldState),
        this,
        snapshot.asInstanceOf[UndefOr[EarlyComponent[Props, Nothing]#I]]
      )
  }

}

object Component {
  implicit def applySelf[Comp <: Component[Comp], T <: Arg](self: Comp): T = self.apply(self).asInstanceOf[T]
}
