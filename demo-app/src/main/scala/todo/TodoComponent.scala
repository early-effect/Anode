package todo

import diode.Circuit
import anode.{CircuitComponent, ClassSelector}
import todo.model.{Root, TodosCircuit}

abstract class TodoComponent[P, S] extends CircuitComponent[P, Root, S] with ClassSelector {
  override def circuit: Circuit[Root] = TodosCircuit
}
