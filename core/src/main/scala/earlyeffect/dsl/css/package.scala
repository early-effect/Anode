package earlyeffect.dsl

import earlyeffect.dsl.css.Styles.DeclarationConstructor

package object css {
  type D[T] = DeclarationConstructor[T]
  type DS   = D[String]
}
