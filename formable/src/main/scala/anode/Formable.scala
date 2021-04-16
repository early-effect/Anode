package anode

import anode.Formable._
import magnolia.{CaseClass, Magnolia}
import org.scalajs.dom

import scala.language.experimental.macros

sealed trait Formable[A] { self =>
  def apply(props: Props[A, _]): Args

  def form(p: Props[A, A]): VNode =
    E.form(
      self(p)
    )
}

object Formable {
  case class Props[Field, Product](label: String, field: Field, update: Field => Product)

  object Props {

    def apply[Product, Result](product: Product, effect: Product => Result) =
      new Props[Product, Product](
        label = "form",
        product,
        x => {
          effect(x)
          x
        },
      )
  }
  type Typeclass[A] = Formable[A]

  implicit class FormableProduct[A](a: A)(implicit f: Formable[A]) {
    def form[T](effect: A => T): anode.VNode = f.form(Props[A, T](a, effect))
    def apply[T](effect: A => T): anode.Args = f(Props[A,T](a,effect))
  }

  def apply[A](f: Props[A, _] => Args): Formable[A] =
    new Formable[A] { override def apply(props: Props[A, _]): Args = f(props) }

  def combine[A](caseClass: CaseClass[Typeclass, A]): Formable[A] =
    Formable { props =>
      args(caseClass.parameters.map { p =>
        val update: p.PType => A = (x: p.PType) => {
          val product = caseClass.construct { cp =>
            if (cp == p) x
            else cp.dereference(props.field)
          }
          props.update(product)
          product
        }
        p.typeclass(Props(p.label, p.dereference(props.field), update))
      })
    }
  implicit def formable[A]: Formable[A] = macro Magnolia.gen[A]
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
