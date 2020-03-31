package todo.a

import diode.Circuit
import earlyeffect.{CircuitComponent, ClassSelector}
import todo.model.{ModelCircuit, Root}

abstract class TodoComponent[P, S] extends CircuitComponent[P, Root, S] with ClassSelector {
  override def circuit: Circuit[Root] = ModelCircuit
}
