package earlyeffect

import earlyeffect.dictionaryNames._

import scala.scalajs.js

sealed trait InstanceOps[Props, State] {
  def props: Props
  def state: State
  def setState(s: State): Unit
}

abstract class BaseInstance[Props, +Component <: ComponentOps[Props, State], State] extends impl.ComponentJS { self =>

  object instance extends InstanceOps[Props, State] {
    def props: Props             = lookupProps()
    def state: State             = lookupState()
    def setState(s: State): Unit = setComponentState(s)
  }

  def componentDidMount(): Unit =
    lookupComponent().didMount(self.instance)

  def componentWillMount(): Unit =
    lookupComponent().willMount(self.instance)

  def componentWillUnmount(): Unit = lookupComponent().willUnMount(self.instance)

  protected final def cast[T](d: js.Dynamic, field: String): T = d.selectDynamic(field).asInstanceOf[T]

  protected final def lookupProps(p: js.Dynamic = rawProps): Props = cast(p, Props)

  protected final def lookupComponent(p: js.Dynamic = rawProps): Component = cast(p, ComponentConstructor)

  protected final def lookupState(s: js.Dynamic = rawState): State = cast(s, State)

  protected def setComponentState(state: State): Unit =
    rawSetState(js.Dictionary(dictionaryNames.State -> state.asInstanceOf[js.Any]).asInstanceOf[js.Dynamic])

}
