package earlyeffect.dsl.css

object values {

  sealed trait Value { self =>

    def nameAsDashedLower: String =
      self.getClass.getSimpleName.foldLeft("") {
        case (s, c) if c.isUpper => s + "-" + c.toLower
        case (s, c)              => s + c
      }

    def value: String = nameAsDashedLower

    override def toString: String = self.value
  }

  sealed trait Color extends Value

  case class rgb(r: Int, g: Int, b: Int) extends Color {
    override def value = s"rgb($r,$g,$b)"
  }

  sealed trait LineStyle extends Value

  object LineStyle {
    case object none   extends LineStyle
    case object dashed extends LineStyle
  }

  sealed abstract class LineWidth(d: Double, suffix: String) extends Value {
    override lazy val value: String = s"$d$suffix"
  }

  object LineWidth {
    case object zero                               extends LineWidth(0, "")
    case class Suffixed(d: Double, suffix: String) extends LineWidth(d, suffix)
  }

  case object foo extends Value

}
