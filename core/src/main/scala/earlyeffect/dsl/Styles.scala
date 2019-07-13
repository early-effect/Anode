package earlyeffect.dsl

import earlyeffect.impl.Predicated

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.|

object Styles {

  sealed trait DeclarationOrSelector {
    def mkString(className: String, keyFrames: js.Array[KeyFrames] = js.Array()): String

    override def toString: String = mkString("")
  }

  private[dsl] case class KeyFrames(name: String, selectors: KeyframeSelector*) extends DeclarationOrSelector {
    override def mkString(className: String, keyFrames: js.Array[KeyFrames]): String = {
      keyFrames.push(this)
      s"animation-name: $className-$name;"
    }
  }

  private[dsl] case class Declaration(property: String, value: String) extends DeclarationOrSelector {
    override def mkString(className: String, kf: js.Array[KeyFrames]): String = s"$property: $value;"
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

  case class KeyframeSelector(selector: String, members: Declaration*) extends DeclarationOrSelector {
    override def mkString(className: String, keyFrames: js.Array[KeyFrames]): String =
      s"$selector {\n${members.map("    " + _.mkString(className, keyFrames)).mkString("\n")}\n  }"
  }

  class Selector private (val selector: String, val members: Seq[DeclarationOrSelector]) extends DeclarationOrSelector {

    override def mkString(className: String, ks: js.Array[KeyFrames]): String = {
      val ss = members.collect { case s: Selector => s }.map(s => s.mkString(className, ks)).mkString("\n")
      val inner = members
        .collect { case p @ (_: Declaration | _: KeyFrames | _: KeyframeSelector) => p }
        .map("  " + _.mkString(className, ks))
        .mkString("\n")
      s"$selector {\n$inner\n}\n" + ss
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
    def s(d: Double): Declaration  = apply(s"${d}s")
    def ms(d: Double): Declaration = apply(s"${d}ms")
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

  trait DisplayOutside extends DS {
    def block: Declaration  = apply("block")
    def inline: Declaration = apply("inline")
    def runIn: Declaration  = apply("run-in")
  }

  trait DisplayBox extends DS with None {
    def contents: Declaration = apply("contents")
  }

  trait DisplayInside extends DS {
    def flow: Declaration     = apply("flow")
    def flowRoot: Declaration = apply("flow-root")
    def table: Declaration    = apply("table")
    def flex: Declaration     = apply("flex")
    def grid: Declaration     = apply("grid")
    def ruby: Declaration     = apply("ruby")
  }

  trait DisplayInternal extends DS {
    def tableRowGroup: Declaration     = apply("table-row-group")
    def tableHeaderGroup: Declaration  = apply("table-header-group")
    def tableFooterGroup: Declaration  = apply("table-footer-group")
    def tableRow: Declaration          = apply("table-row")
    def tableCell: Declaration         = apply("table-cell")
    def tableColumnGroup: Declaration  = apply("table-column-group")
    def tableColumn: Declaration       = apply("table-column")
    def tableCaption: Declaration      = apply("table-caption")
    def rubyBase: Declaration          = apply("ruby-base")
    def rubyText: Declaration          = apply("ruby-text")
    def rubyBaseContainer: Declaration = apply("ruby-base-container")
    def rubyTextContainer: Declaration = apply("ruby-text-container")
  }

  trait DisplayLegacy extends DS {
    def inlineBlock: Declaration    = apply("inline-block")
    def inlineListItem: Declaration = apply("inline-list-item")
    def inlineTable: Declaration    = apply("inline-table")
    def inlineFlex: Declaration     = apply("inline-flex")
    def inlineGrid: Declaration     = apply("inline-grid")
  }

  trait DisplayListItem extends DS {
    def block_listItem: Declaration           = apply("block list-item")
    def block_flow_listItem: Declaration      = apply("block flow list-item")
    def block_flowRoot_listItem: Declaration  = apply("block flow-root list-item")
    def inline_listItem: Declaration          = apply("inline list-item")
    def inline_flow_listItem: Declaration     = apply("inline flow list-item")
    def inline_flowRoot_listItem: Declaration = apply("inline flow-root list-item")
    def runIn_listItem: Declaration           = apply("run-in list-item")
    def runIn_flow_listItem: Declaration      = apply("run-in flow list-item")
    def runIn_flowRoot_listItem: Declaration  = apply("run-in flow-root list-item")
  }

  trait Display
      extends DisplayBox
      with DisplayOutside
      with DisplayInside
      with DisplayInternal
      with DisplayLegacy
      with DisplayListItem

  def animation: D[String]               = Declaration("animation")
  def animationDelay                     = new DeclarationConstructor[String]("animation-delay") with Time {}
  def animationDirection: D[String]      = Declaration("animation-direction")
  def animationDuration                  = new DeclarationConstructor[String]("animation-duration") with Time {}
  def animationFillMode: D[String]       = Declaration("animation-fill-mode")
  def animationIterationCount: D[String] = Declaration("animation-iteration-count")
  def animationName                      = new DeclarationConstructor[String]("animation-name") with None {}
  def animationPlayState: D[String]      = Declaration("animation-play-state")
  def animationTimingFunction: D[String] = Declaration("animation-timing-function")

  lazy val color: DS with Color = new DeclarationConstructor[String]("color") with Color {}
  lazy val zIndex: D[Int]       = Declaration("z-index")
  lazy val all: DS = new DeclarationConstructor[String]("all") {
    def initial: Declaration = apply("initial")
    def inherit: Declaration = apply("inherit")
    def unset: Declaration   = apply("unset")
    def revert: Declaration  = apply("revert")
  }
  def alignContent: DS = Declaration("align-content")

  def alignItems: DS = Declaration("align-items")

  def alignSelf: DS = Declaration("align-self")

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

  //TODO:Incomplete
  lazy val justifyContent = new DeclarationConstructor[String]("justify-content") with Normal {
    def left: Declaration  = apply("left")
    def right: Declaration = apply("right")
  }

  lazy val lineHeight = new DeclarationConstructor[String]("line-height") with Normal with LengthPercentage {
    def number(d: Double): Declaration = apply(s"$d")
  }

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

  def textShadow: DS = Declaration("text-shadow")

  //TODO: This is not a complete type
  lazy val height = new DeclarationConstructor[String]("height") with Auto with LengthPercentage {
    def available: Declaration  = apply("available")
    def minContent: Declaration = apply("min-content")
    def maxContent: Declaration = apply("max-content")
    def fitContent: Declaration = apply("fit-content")
  }
  //TODO: This is not a complete type
  lazy val width = new DeclarationConstructor[String]("width") with Auto with LengthPercentage {
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

  lazy val cursor = new DeclarationConstructor[String]("cursor") with Auto with None {
    def default: Declaration      = apply("default")
    def contextMenu: Declaration  = apply("context-menu")
    def help: Declaration         = apply("help")
    def pointer: Declaration      = apply("pointer")
    def progress: Declaration     = apply("progress")
    def waitCursor: Declaration   = apply("wait")
    def cell: Declaration         = apply("cell")
    def crosshair: Declaration    = apply("crosshair")
    def text: Declaration         = apply("text")
    def verticalText: Declaration = apply("vertical-text")
    def alias: Declaration        = apply("alias")
    def copy: Declaration         = apply("copy")
    def move: Declaration         = apply("move")
    def noDrop: Declaration       = apply("no-drop")
    def notAllowed: Declaration   = apply("not-allowed")
    def eResize: Declaration      = apply("e-resize")
    def nResize: Declaration      = apply("n-resize")
    def neResize: Declaration     = apply("ne-resize")
    def nwResize: Declaration     = apply("nw-resize")
    def sResize: Declaration      = apply("s-resize")
    def seResize: Declaration     = apply("se-resize")
    def swResize: Declaration     = apply("sw-resize")
    def wResize: Declaration      = apply("w-resize")
    def ewResize: Declaration     = apply("ew-resize")
    def nsResize: Declaration     = apply("ns-resize")
    def nsewResize: Declaration   = apply("nsew-resize")
    def nwseResize: Declaration   = apply("nwse-resize")
    def colResize: Declaration    = apply("col-resize")
    def rowResize: Declaration    = apply("row-resize")
    def allScroll: Declaration    = apply("all-scroll")
    def zoomIn: Declaration       = apply("zoom-in")
    def zoomOut: Declaration      = apply("zoom-out")
    def grab: Declaration         = apply("grab")
    def grabbing: Declaration     = apply("grabbing")
  }

  def padding: DS = Declaration("padding")

  lazy val paddingLeft   = new DeclarationConstructor[String]("padding-left") with LengthPercentage   {}
  lazy val paddingRight  = new DeclarationConstructor[String]("padding-right") with LengthPercentage  {}
  lazy val paddingTop    = new DeclarationConstructor[String]("padding-top") with LengthPercentage    {}
  lazy val paddingBottom = new DeclarationConstructor[String]("padding-bottom") with LengthPercentage {}

  def opacity: D[Double] = Declaration("opacity")

  lazy val display = new DeclarationConstructor[String]("display") with Display {}

  def flex: DS = Declaration("flex")

  def flexBasis: DS = Declaration("flex-basis")

  trait FlexDirection extends DS {
    def row: Declaration           = apply("row")
    def rowReverse: Declaration    = apply("row-reverse")
    def column: Declaration        = apply("column")
    def columnReverse: Declaration = apply("column-reverse")
  }

  trait FlexWrap extends DS {
    def nowrap: Declaration      = apply("nowrap")
    def wrap: Declaration        = apply("wrap")
    def wrapReverse: Declaration = apply("wrap-reverse")
  }

  lazy val flexDirection = new DeclarationConstructor[String]("flex-direction") with FlexDirection          {}
  lazy val flexFlow      = new DeclarationConstructor[String]("flex-flow") with FlexDirection with FlexWrap {}

  def flexGrow: D[Double] = Declaration("flex-grow")

  def flexShrink: D[Double] = Declaration("flex-shrink")

  lazy val flexWrap = new DeclarationConstructor[String]("flex-wrap") with FlexWrap {}

  lazy val float = new DeclarationConstructor[String]("float") with None {
    def left: Declaration        = apply("left")
    def right: Declaration       = apply("right")
    def inlineStart: Declaration = apply("inline-start")
    def inlineEnd: Declaration   = apply("inline-end")
  }

  def fontFamily: DS = Declaration("font-family")

  def fontFeatureSettings: DS = Declaration("font-feature-settings")

  lazy val fontKerning = new DeclarationConstructor[String]("font-kerning") with Auto with Normal with None {}

  lazy val fontLanguageOverride = new DeclarationConstructor[String]("font-language-override") with Normal {}

  lazy val fontOpticalSizing = new DeclarationConstructor[String]("font-optical-sizing") with Auto with None {}

  lazy val fontSize =
    new DeclarationConstructor[String]("font-size") with AbsoluteSize with RelativeSize with LengthPercentage {}

  lazy val fontSizeAdjust = new DeclarationConstructor[String]("font-size-adjust") with None {
    def number(d: Double): Declaration = apply(s"$d")
  }

  trait FontStretchAbsolute extends Normal with Percent {
    def ultraCondensed: Declaration = apply("ultra-condensed")
    def extraCondensed: Declaration = apply("extra-condensed")
    def condensed: Declaration      = apply("condensed")
    def semCondensed: Declaration   = apply("sem-condensed")
    def semiExpanded: Declaration   = apply("semi-expanded")
    def expanded: Declaration       = apply("expanded")
    def extraExpanded: Declaration  = apply("extra-expanded")
    def ultraExpanded: Declaration  = apply("ultra-expanded")

  }
  lazy val fontStretch = new DeclarationConstructor[String]("font-stretch") with FontStretchAbsolute {}

  lazy val fontStyle = new DeclarationConstructor[String]("font-style") with Normal {
    def italic: Declaration = apply("italic")
    def oblique             = Oblique

    object Oblique {
      private def property(d: Double, suffix: String) = apply(s"oblique $d$suffix")
      def deg(d: Double)                              = property(d, "deg")
      def rad(d: Double)                              = property(d, "rad")
      def grad(d: Double)                             = property(d, "grad")
      def turn(d: Double)                             = property(d, "turn")
    }
  }

  lazy val fontSynthesis = new DeclarationConstructor[String]("font-synthesis") with None {
    def weight: Declaration = apply("weight")
    def style: Declaration  = apply("style")
  }

  //TODO: this is incomplete - I think I could do this one
  lazy val fontVariant = new DeclarationConstructor[String]("font-variant") with Normal with None {}

  //TODO: this is incomplete - I think I could do this one
  lazy val fontVariantAlternatives = new DeclarationConstructor[String]("font-variant-alternatives") with Normal
  with None {}

  lazy val fontVariantCaps = new DeclarationConstructor[String]("font-variant-caps") with Normal {
    def smallCaps: Declaration     = apply("small-caps")
    def allSmallCaps: Declaration  = apply("all-small-caps")
    def petiteCaps: Declaration    = apply("petite-caps")
    def allPetiteCaps: Declaration = apply("all-petite-caps")
    def unicase: Declaration       = apply("unicase")
    def titlingCaps: Declaration   = apply("titling-caps")
  }

  lazy val fontVariantPosition = new DeclarationConstructor[String]("font-variant-position") with Normal {
    def sub: Declaration     = apply("sub")
    def `super`: Declaration = apply("super")
  }

  lazy val fontWeight = new DeclarationConstructor[String]("font-weight") with FontWeightAbsolute {
    def bolder: Declaration  = apply("bolder")
    def lighter: Declaration = apply("lighter")
  }

  lazy val marginLeft   = new DeclarationConstructor[String]("margin-left") with Auto with LengthPercentage   {}
  lazy val marginRight  = new DeclarationConstructor[String]("margin-right") with Auto with LengthPercentage  {}
  lazy val marginTop    = new DeclarationConstructor[String]("margin-top") with Auto with LengthPercentage    {}
  lazy val marginBottom = new DeclarationConstructor[String]("margin-bottom") with Auto with LengthPercentage {}

  def rgb(r: Int, g: Int, b: Int): String             = s"rgb($r,$g,$b)"
  def rgba(r: Int, g: Int, b: Int, a: Double): String = s"rgb($r,$g,$b,$a)"

}
