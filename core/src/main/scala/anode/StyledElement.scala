package anode

import anode.dsl.ElementConstructor
import anode.dsl.css.CssClass
import anode.dsl.css.Styles.DeclarationOrSelector

abstract class StyledElement(elementConstructor: ElementConstructor)(ds: DeclarationOrSelector*)
    extends CssClass(ds: _*) {
  def apply(as: Arg*): VNode = elementConstructor(args(as = this, as))
}
