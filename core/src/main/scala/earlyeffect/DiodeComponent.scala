package earlyeffect

import diode.{Circuit, FastEq, ModelR}
import earlyeffect.impl.{DiodeComponentInstance, EarlyEffect}

import scala.language.implicitConversions
import scala.scalajs.js

trait DiodeComponent[Props, M <: AnyRef, State] { self =>

  def circuit: Circuit[M]
  def reader(p: Props): ModelR[M, State]

  def zoom(get: M => State)(implicit f: FastEq[_ >: State]): ModelR[M, State] = circuit.zoom(get)

  type I = DiodeComponentInstance[Props, M, State]
  def render(props: Props, state: State): VNode

  def didMount(instance: I): Unit                                    = ()
  def willMount(instance: I): Unit                                   = ()
  def didUpdate(oldProps: Props, oldState: State, instance: I): Unit = ()

  // we might want to do a deep equality check?
  def shouldUpdate(nextProps: Props, nextState: State, previous: I): Boolean =
    previous.props != nextProps || nextState != previous.fetchState()

  val constructor: js.Dynamic =
    Component.constructors.getOrElseUpdate(self.getClass.getName, js.constructorOf[I])

  def apply(props: Props): VNode =
    EarlyEffect.h(
      constructor,
      js.Dictionary[js.Any](
        ("p1", props.asInstanceOf[js.Any]),
        ("cc", self.asInstanceOf[js.Any])
      )
    )
}
