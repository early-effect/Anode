package earlyeffect

import earlyeffect.impl.{ComponentJS, EarlyEffect, VNodeJS}

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

abstract class Component[Props] { self =>

  type I = Component.Instance[Props]

  def render(props: Props): VNode

  def didMount(instance: I): Unit                   = ()
  def willMount(instance: I): Unit                  = ()
  def didUpdate(oldProps: Props, instance: I): Unit = ()

  // we might want to do a deep equality check?
  def shouldUpdate(nextProps: Props, previous: I): Boolean =
    previous.props != nextProps

  val constructor: js.Dynamic =
    constructors.getOrElseUpdate(this.getClass.getName, js.constructorOf[I])

  val defaultKey = self.getClass.getName

  def apply(props: Props): VNode =
    EarlyEffect.h(
      constructor,
      js.Dictionary[js.Any](
        ("key", defaultKey),
        ("p1", props.asInstanceOf[js.Any]),
        ("cc", self.asInstanceOf[js.Any])
      )
    )
}

object Component {

  class Instance[Props] extends ComponentJS {

    def component(props: js.Dynamic = rawProps): earlyeffect.Component[Props] =
      props.cc.asInstanceOf[earlyeffect.Component[Props]]

    def lookup(props: js.Dynamic = rawProps): Props = props.p1.asInstanceOf[Props]

    @JSName("richProps")
    def props = lookup()

    override def render(props: js.Dynamic, state: js.Dynamic): VNodeJS = component(props).render(lookup(props)).vn

    def componentDidMount(): Unit =
      component().didMount(this)

    def componentWillMount(): Unit =
      component().willMount(this)

    def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      component().didUpdate(lookup(oldProps), this)

    def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, context: js.Dynamic): Boolean =
      component().shouldUpdate(lookup(nextProps), this)

  }

  implicit def applySelf[Comp <: Component[Comp], T <: Arg](self: Comp): T = self.apply(self).asInstanceOf[T]
}
