package anode.formable

import anode._
import munit.FunSuite

class FormableSpecs extends FunSuite with AnodeOps {
  type Form[A] = Formable.Context[A, A]

  object Form {
    def apply[A](a: A): Form[A] = Formable(s"label-$a", a)(identity)

    def apply[A]: Formable[A] = Formable[A](a => args(text((a.label, a.field.toString).toString())))
  }

  implicit val formString: Formable[String] = Form[String]
  implicit val formInt: Formable[Int]       = Form[Int]

  case class Foo(a: String, b: Int)

  test("Formable String") {
    render(Form("a"))
    check("<div>(label-a,a)</div>")
  }
  test("Formable int") {
    render(Form(1))
    check("<div>(label-1,1)</div>")
  }
  test("Formable Foo") {
    render(Form(Foo("foo", 1)))
    check("<div>(a,foo)(b,1)</div>")
  }
}
