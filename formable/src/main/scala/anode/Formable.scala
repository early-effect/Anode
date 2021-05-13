package anode

import anode.Formable._
import magnolia.{CaseClass, Magnolia, SealedTrait}
import org.scalajs.dom

import scala.language.experimental.macros
import scala.language.implicitConversions

sealed trait Formable[A] { self =>
  def apply(context: Context[A, _]): Args

  def form(context: Context[A, A]): VNode =
    E.form(
      self(context)
    )
}

object Formable {
  type Typeclass[A] = Formable[A]

  case class Context[Field, Product] private[anode] (label: String, field: Field, update: Field => Product)

  def apply[Field, Product](label: String, field: Field)(update: Field => Product): Context[Field, Product] =
    Context(label, field, update)

  def apply[Product, Result](product: Product)(effect: Product => Result): Context[Product, Product] =
    Context[Product, Product](
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

  def apply[A](f: Context[A, _] => Args): Formable[A] =
    new Formable[A] { override def apply(context: Context[A, _]): Args = f(context) }

  def combine[Product](caseClass: CaseClass[Typeclass, Product]): Formable[Product] =
    Formable { context =>
      args(caseClass.parameters.map { productParam =>
        type ParamType = productParam.PType
        val update: ParamType => Product = (x: ParamType) => {
          val product = caseClass.construct { constructorParam =>
            if (constructorParam == productParam) x
            else constructorParam.dereference(context.field)
          }
          context.update(product)
          product
        }
        productParam.typeclass(apply(productParam.label, productParam.dereference(context.field))(update))
      })
    }

  def dispatch[Product](sealedTrait: SealedTrait[Formable, Product]): Formable[Product] =
    Formable { context =>
      sealedTrait.dispatch(context.field) { subtype =>
        type S = subtype.SType
        subtype.typeclass.apply(Formable[S,Product](context.label, subtype.cast(context.field))((s:S) => {
          context.update(s)
          s
        }))
      }
    }

  implicit def formable[A]: Formable[A] = macro Magnolia.gen[A]

  implicit def summonArgsFromProps[Field](context: Context[Field, _])(implicit formable: Formable[Field]): Args =
    formable(context)

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
