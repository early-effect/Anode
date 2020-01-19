package earlyeffect

import earlyeffect.dictionaryNames._
import earlyeffect.impl.VNodeJS
import org.scalajs.dom.Element

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSName

/**
  * Provides a type safe view of the underlying instance.
  *
  * When we pass things to components we don't want them to see the life cycle function or the raw JS "types"
  *
  * @tparam Props
  * @tparam State
  */
trait EarlyInstance[Props, State] extends js.Any {

  @JSName("eProps")
  def props: Props

  @JSName("eState")
  def state: State

  @JSName("eSetState")
  def setState(s: State): Unit

  @JSName("eBase")
  def base: UndefOr[Element]

}

abstract class InstanceFacade[Props, State] extends impl.ComponentJS with EarlyInstance[Props, State] {
  theFacade =>

  @JSName("eBase")
  def base: UndefOr[Element] = rawBase

  @JSName("eProps")
  override def props: Props = lookupProps()

  @JSName("eState")
  override def state: State = lookupState()

  @JSName("eSetState")
  override def setState(s: State): Unit = setComponentState(s)

  def addSelectors(n: VNode, component: EarlyComponent[Props, State]): VNode =
    component match {
      case classSelector: ClassSelector => n.withRef(e => classSelector.addClass(e))
      case instanceDataSelector: InstanceDataSelector =>
        n.withRef(e => instanceDataSelector.addDataAttribute(e, theFacade))
      case _ => n
    }

  protected final def cast[T](d: js.Dynamic, field: String): T = d.selectDynamic(field).asInstanceOf[T]

  protected final def lookupProps(p: js.Dynamic = rawProps): Props = cast(p, PropsFieldName)

  protected final def lookupState(s: js.Dynamic = rawState): State = cast(s, StateFieldName)

  protected final def setComponentState(state: State): Unit =
    rawSetState(js.Dictionary(dictionaryNames.StateFieldName -> state.asInstanceOf[js.Any]).asInstanceOf[js.Dynamic])

  def componentWillReceiveProps(nextProps: js.Dynamic, nextState: js.Dynamic): Unit = ()

}
