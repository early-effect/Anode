package earlyeffect

import earlyeffect.impl.VNodeJS

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSName

trait Component[Props] extends StatelessComponent[Props]

trait StatelessComponent[Props] extends EarlyComponent[Props, Nothing] { theComponent =>

  def render(props: Props): VNode

  def didUpdate(oldProps: Props, instance: Instance, oldInstance: UndefOr[Instance]): Unit = ()

  def shouldUpdate(nextProps: Props, previous: Instance)(implicit propsEQV: Equivalence[_ >: Props]): Boolean =
    propsEQV.notEquivalent(previous.props, nextProps)

  override lazy val instanceConstructor: js.Dynamic = js.constructorOf[StatelessInstance]

  final private class StatelessInstance extends InstanceFacade[Props, Nothing] {

    override def componentDidMount(): Unit = didMount(this)

    override def componentWillMount(): Unit = willMount(this)

    override def componentWillUnmount(): Unit = willUnMount(this)

    @JSName("render")
    override def renderJS(props: js.Dynamic, state: js.Dynamic): VNodeJS =
      addSelectors(render(lookupProps(props)), this)

    override def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, nextContext: js.Dynamic): Boolean =
      shouldUpdate(lookupProps(nextProps), this)

    override def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      didUpdate(
        lookupProps(oldProps),
        lookupState(oldState),
        this,
        snapshot.asInstanceOf[UndefOr[EarlyComponent[Props, Nothing]#Instance]]
      )
  }

}

object StatelessComponent {
  implicit def applySelf[Comp <: Component[Comp], T <: Arg](self: Comp): T = self.apply(self).asInstanceOf[T]
}
