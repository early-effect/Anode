package earlyeffect

import earlyeffect.dictionaryNames._
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
sealed trait EarlyInstance[Props, State] extends js.Any {

  @JSName("eProps")
  def props: Props

  @JSName("eState")
  def state: State

  @JSName("eSetState")
  def setState(s: State): Unit

  @JSName("eBase")
  def base: UndefOr[Element]

}

abstract class InstanceFacade[Props, +Component <: EarlyComponent[Props, State], State]
    extends impl.ComponentJS
    with EarlyInstance[Props, State] {
  self =>

  @JSName("eBase")
  def base: UndefOr[Element] = rawBase

  val instance: EarlyInstance[Props, State] = self

  @JSName("eProps")
  override def props: Props = lookupProps()

  @JSName("eState")
  override def state: State = lookupState()

  @JSName("eSetState")
  override def setState(s: State): Unit = setComponentState(s)

  def componentDidMount(): Unit = {
    lookupComponent() match {
      case cs: ClassSelector => cs.addClass(self)
      case _                 => ()
    }
    lookupComponent() match {
      case pds: InstanceDataSelector => pds.addDataAttribute(self)
      case _                         => ()
    }
    lookupComponent().didMount(self)
  }

  def componentWillMount(): Unit =
    lookupComponent().willMount(self)

  def componentWillUnmount(): Unit = lookupComponent().willUnMount(self)

  protected final def cast[T](d: js.Dynamic, field: String): T = d.selectDynamic(field).asInstanceOf[T]

  protected final def lookupProps(p: js.Dynamic = rawProps): Props = cast(p, Props)

  protected final def lookupComponent(p: js.Dynamic = rawProps): Component = cast(p, ComponentConstructor)

  protected final def lookupState(s: js.Dynamic = rawState): State = cast(s, State)

  protected final def setComponentState(state: State): Unit =
    rawSetState(js.Dictionary(dictionaryNames.State -> state.asInstanceOf[js.Any]).asInstanceOf[js.Dynamic])

}
