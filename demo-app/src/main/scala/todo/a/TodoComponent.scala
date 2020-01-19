package todo.a

import diode.Circuit
import earlyeffect.CircuitComponent
import todo.model.{ModelCircuit, Root}

abstract class TodoComponent[P, S] extends CircuitComponent[P, Root, S] {
  override def circuit: Circuit[Root] = ModelCircuit
}
