package earlyeffect

import earlyeffect.dsl.ElementConstructor
import earlyeffect.dsl.css.CssClass
import earlyeffect.dsl.css.Styles.DeclarationOrSelector

abstract class StyledElement(elementConstructor: ElementConstructor)(ds: DeclarationOrSelector*)
    extends CssClass(ds: _*) {
  def apply(as: Arg*): VNode = elementConstructor(args(as = this, as))
}
