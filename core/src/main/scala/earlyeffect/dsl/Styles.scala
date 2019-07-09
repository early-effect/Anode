package earlyeffect.dsl

import earlyeffect.impl.Predicated

import scala.language.implicitConversions

object Styles {

  sealed trait DeclarationOrSelector {
    def mkString: String
  }

  private[dsl] case class Declaration(property: String, value: String) extends DeclarationOrSelector {
    override def mkString: String = s"$property: $value;"
  }

  sealed abstract class DeclarationConstructor[T](property: String) {
    def apply(value: T) = Declaration(property, value.toString)
  }

  private[dsl] case class Constructor[T](property: String) extends DeclarationConstructor[T](property)

  object Declaration {
    def apply[T](property: String): DeclarationConstructor[T] = Constructor[T](property)

    implicit class When(p: Declaration) extends Predicated(p)
  }

  type D[T] = DeclarationConstructor[T]
  type DS   = D[String]

  def apply(name: String, value: String)            = Declaration(name, value)
  def apply[T](name: String, value: T): Declaration = Declaration(name, value.toString)

  class Selector private (val selector: String, val members: Seq[DeclarationOrSelector]) extends DeclarationOrSelector {

    override def mkString: String = {
      val ss = members.collect { case s: Selector => s }.map(s => s.mkString).mkString("\n")
      s"$selector {\n${members.collect { case p: Declaration => p }.map("  " + _.mkString).mkString("\n")}\n}\n" + ss
    }

    def prependAll(s: String): Selector =
      new Selector(s + selector, members.map {
        case x: Selector => x.prependAll(s)
        case y           => y
      })
  }

  object Selector {

    def apply(selector: String, members: DeclarationOrSelector*): Selector =
      new Selector(
        selector,
        members.map {
          case x: Selector => x.prependAll(selector)
          case x           => x
        }
      )
  }

  trait Normal extends DS {
    def normal: Declaration = apply("normal")
  }

  trait None extends DS {
    def none: Declaration = apply("none")
  }

  trait Auto extends DS {
    def auto: Declaration = apply("auto")
  }

  trait Compat extends DS {
    def searchfield: Declaration    = apply("searchfield")
    def textarea: Declaration       = apply("textarea")
    def pushButton: Declaration     = apply("push-button")
    def buttonBevel: Declaration    = apply("button-bevel")
    def checkbox: Declaration       = apply("checkbox")
    def radio: Declaration          = apply("radio")
    def squareButton: Declaration   = apply("square-button")
    def menulist: Declaration       = apply("menulist")
    def menu: Declaration           = apply("menu")
    def menulistButton: Declaration = apply("menulist-button")
    def listbox: Declaration        = apply("listbox")
    def meter: Declaration          = apply("meter")
    def progressBar: Declaration    = apply("progress-bar")
  }

  trait Suffixed[T] extends DS {
    type ST = T => Declaration
    def suffixed(s: String)(t: T): Declaration = apply(s"$t$s")
  }

  trait FontRelativeLength extends Suffixed[Double] {
    def cap: ST = suffixed("cap")
    def ch: ST  = suffixed("ch")
    def em: ST  = suffixed("em")
    def ex: ST  = suffixed("ex")
    def ic: ST  = suffixed("ic")
    def lh: ST  = suffixed("lh")
    def rem: ST = suffixed("rem")
    def rlh: ST = suffixed("rlh")
  }

  trait ViewportPercentageLength extends Suffixed[Double] {
    def vh: ST   = suffixed("vh")
    def vw: ST   = suffixed("vw")
    def vi: ST   = suffixed("vi")
    def vb: ST   = suffixed("vb")
    def vmin: ST = suffixed("vmin")
    def vmax: ST = suffixed("vmax")
  }

  trait AbsoluteLength extends Suffixed[Double] {
    def px: ST = suffixed("px")
    def cm: ST = suffixed("cm")
    def mm: ST = suffixed("mm")
    def q: ST  = suffixed("Q")
    def in: ST = suffixed("in")
    def pc: ST = suffixed("pc")
    def pt: ST = suffixed("pt")
  }

  trait Length extends FontRelativeLength with ViewportPercentageLength with AbsoluteLength

  trait Color extends DS {
    def rgb(r: Int, g: Int, b: Int): Declaration             = apply(s"rgb($r,$g,$b)")
    def rgba(r: Int, g: Int, b: Int, a: Double): Declaration = apply(s"rgb($r,$g,$b,$a)")
  }

  trait Number extends DS {
    def apply(n: Int): Declaration    = apply(n.toString)
    def apply(d: Double): Declaration = apply(d.toString)
  }

  trait NumberOrPercentage extends Number with Percent

  trait Percent extends DS {
    def percent(d: Double): Declaration = apply(s"$d%")
    def pct(d: Double): Declaration     = percent(d)
  }

  trait Time extends DS {
    def s(d: Double): Declaration  = apply(s"{$d}s")
    def ms(d: Double): Declaration = apply(s"{$d}ms")
  }

  trait LineWidth extends DS with Length {
    def thin: Declaration   = apply("thin")
    def medium: Declaration = apply("medium")
    def thick: Declaration  = apply("thick")
  }

  trait Hidden extends DS {
    def hidden: Declaration = apply("hidden")
  }

  trait Visible extends DS {
    def visible: Declaration = apply("visible")
  }

  trait HiddenOrVisible extends Hidden with Visible

  trait LinStyle extends DS with None with Hidden {
    def dotted: Declaration = apply("dotted")
    def dashed: Declaration = apply("dashed")
    def solid: Declaration  = apply("solid")
    def double: Declaration = apply("double")
    def groove: Declaration = apply("groove")
    def ridge: Declaration  = apply("ridge")
    def inset: Declaration  = apply("inset")
    def outset: Declaration = apply("outset")
  }

  trait LeaderType extends DS {
    def dotted: Declaration = apply("dotted")
    def solid: Declaration  = apply("solid")
    def space: Declaration  = apply("space")
  }

  trait Leader extends DS with LeaderType {
    override def apply(value: String): Declaration = super.apply(s"leader($value)")
  }

  trait LengthPercentage extends Length with Percent

  trait Attachment extends DS {
    def scroll: Declaration = apply("scroll")
    def fixed: Declaration  = apply("fixed")
    def local: Declaration  = apply("local")
  }

  trait BlendMode extends DS with Normal {
    def multiply: Declaration   = apply("multiply")
    def screen: Declaration     = apply("screen")
    def overlay: Declaration    = apply("overlay")
    def darken: Declaration     = apply("darken")
    def lighten: Declaration    = apply("lighten")
    def colorBurn: Declaration  = apply("color-burn")
    def hardLight: Declaration  = apply("hard-light")
    def softLight: Declaration  = apply("soft-light")
    def difference: Declaration = apply("difference")
    def exclusion: Declaration  = apply("exclusion")
    def hue: Declaration        = apply("hue")
    def saturation: Declaration = apply("saturation")
    def color: Declaration      = apply("color")
    def luminosity: Declaration = apply("luminosity")
  }

  trait Box extends DS {
    def borderBox: Declaration  = apply("border-box")
    def paddingBox: Declaration = apply("padding-box")
    def contentBox: Declaration = apply("content-box")
  }

  trait FontWeightAbsolute extends DS with Number {
    def normal: Declaration = apply("normal")
    def bold: Declaration   = apply("bold")
  }

  trait AbsoluteSize extends DS {
    def xxSmall: Declaration = apply("xx-small")
    def xSmall: Declaration  = apply("x-small")
    def small: Declaration   = apply("small")
    def medium: Declaration  = apply("medium")
    def large: Declaration   = apply("large")
    def xLarge: Declaration  = apply("x-large")
    def xxLarge: Declaration = apply("xx-large")
  }

  trait RelativeSize extends DS {
    def smaller: Declaration = apply("smaller")
    def larger: Declaration  = apply("larger")
  }

  def animation               = Declaration("animation")
  def animationDelay          = new DeclarationConstructor[String]("animation-delay") with Time {}
  def animationDirection      = Declaration("animation-direction")
  def animationDuration       = new DeclarationConstructor[String]("animation-duration") with Time {}
  def animationFillMode       = Declaration("animation-fill-mode")
  def animationIterationCount = Declaration("animation-iteration-count")
  def animationName           = new DeclarationConstructor[String]("animation-name") with None {}
  def animationPlayState      = Declaration("animation-play-state")
  def animationTimingFunction = Declaration("animation-timing-function")

  lazy val color: DS with Color = new DeclarationConstructor[String]("color") with Color {}
  lazy val zIndex: D[Int]       = Declaration("z-index")
  lazy val all: DS = new DeclarationConstructor[String]("all") {
    def initial: Declaration = apply("initial")
    def inherit: Declaration = apply("inherit")
    def unset: Declaration   = apply("unset")
    def revert: Declaration  = apply("revert")
  }
  lazy val appearance = new DeclarationConstructor[String]("appearance") with None with Auto with Compat {
    def button: Declaration    = apply("button")
    def textfield: Declaration = apply("textfield")
  }
  lazy val backfaceVisibility             = new DeclarationConstructor[String]("backface-visibility") with HiddenOrVisible {}
  def background                          = Declaration("background")
  lazy val backgroundAttachment           = new DeclarationConstructor[String]("background-attachment") with Attachment {}
  lazy val backgroundBlendMode            = new DeclarationConstructor[String]("background-blend-mode") with BlendMode {}
  lazy val backgroundClip                 = new DeclarationConstructor[String]("background-clip") with Box {}
  lazy val backgroundColor: DS with Color = new DeclarationConstructor[String]("background-color") with Color {}
  def backgroundImage                     = Declaration("background-image")
  lazy val backgroundOrigin               = new DeclarationConstructor[String]("background-origin") with Box {}
  def backgroundPosition: DS              = Declaration("background-position")
  def backgroundRepeat: DS                = Declaration("background-repeat")
  def backgroundSize: DS                  = Declaration("background-size")
  lazy val blockSize                      = new DeclarationConstructor[String]("block-size") with Auto with Length {}
  def border: DS                          = Declaration("border")
  def borderBlock: DS                     = Declaration("border-block")

  def font: DS = Declaration("font")

  lazy val textAlign = new DeclarationConstructor[String]("text-align") {
    def start: Declaration       = apply("start")
    def send: Declaration        = apply("send")
    def left: Declaration        = apply("left")
    def right: Declaration       = apply("right")
    def center: Declaration      = apply("center")
    def justify: Declaration     = apply("justify")
    def matchParent: Declaration = apply("match-parent")
  }

  lazy val textRendering = new DeclarationConstructor[String]("text-rendering") with Auto {
    def optimizeSpeed: Declaration      = apply("optimizeSpeed")
    def optimizeLegibility: Declaration = apply("optimizeLegibility")
    def geometricPrecision: Declaration = apply("geometricPrecision")
  }

  lazy val fontWeight = new DeclarationConstructor[String]("font-weight") with FontWeightAbsolute {
    def bolder: Declaration  = apply("bolder")
    def lighter: Declaration = apply("lighter")
  }

  lazy val fontSize =
    new DeclarationConstructor[String]("font-size") with AbsoluteSize with RelativeSize with LengthPercentage {}

  //TODO: This is not a complete type
  lazy val height = new DeclarationConstructor[String]("height") with Auto with LengthPercentage {
    def available: Declaration  = apply("available")
    def minContent: Declaration = apply("min-content")
    def maxContent: Declaration = apply("max-content")
    def fitContent: Declaration = apply("fit-content")
  }

  lazy val margin = new DeclarationConstructor[String]("margin") {
    def apply(vertical: String, horizontal: String): Declaration            = apply(s"$vertical $horizontal")
    def apply(top: String, horizontal: String, bottom: String): Declaration = apply(s"$top $horizontal $bottom")

    def apply(top: String, right: String, bottom: String, left: String): Declaration =
      apply(s"$top $right $bottom $left")
  }

}
