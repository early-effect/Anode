package anode

import anode.Formable._
import magnolia.{CaseClass, Magnolia}
import org.scalajs.dom

import scala.language.experimental.macros
import scala.language.implicitConversions

sealed trait Formable[A] { self =>
  def apply(props: Props[A, _]): Args

  def form(p: Props[A, A]): VNode =
    E.form(
      self(p)
    )
}

object Formable {
  type Typeclass[A] = Formable[A]

  case class Props[Field, Product] private[anode] (label: String, field: Field, update: Field => Product)

  def apply[Field, Product](label: String, field: Field)(update: Field => Product): Props[Field, Product] =
    Props(label, field, update)

  def apply[Product, Result](product: Product)(effect: Product => Result): Props[Product, Product] =
    Props[Product, Product](
      label = "form",
      product,
      x => {
        effect(x)
        x
      },
    )

  implicit class FormableProduct[Product](product: Product)(implicit f: Formable[Product]) {
    def form[A](effect: Product => A): anode.VNode = f.form(Formable[Product, A](product)(effect))
    def apply[A](effect: Product => A): anode.Args = f(Formable[Product, A](product)(effect))
  }

  def apply[A](f: Props[A, _] => Args): Formable[A] =
    new Formable[A] { override def apply(props: Props[A, _]): Args = f(props) }

  def combine[Product](caseClass: CaseClass[Typeclass, Product]): Formable[Product] =
    Formable { props =>
      args(caseClass.parameters.map { productParam =>
        type ParamType = productParam.PType
        val update: ParamType => Product = (x: ParamType) => {
          val product = caseClass.construct { constructorParam =>
            if (constructorParam == productParam) x
            else constructorParam.dereference(props.field)
          }
          props.update(product)
          product
        }
        productParam.typeclass(apply(productParam.label, productParam.dereference(props.field))(update))
      })
    }
  implicit def formable[A]: Formable[A] = macro Magnolia.gen[A]

  implicit def summonArgsFromProps[Field](props: Props[Field, _])(implicit formable: Formable[Field]): Args =
    formable(props)

  object defaultImplicits {

    implicit val string: Formable[String] = Formable { p =>
      args(
        E.div(
          E.label(p.label),
          E.input(
            A.value(p.field),
            A.onKeyUp { x =>
              p.update(x.target.asInstanceOf[dom.html.Input].value)
            },
          ),
        )
      )
    }
  }

}
