package earlyeffect.dsl

import earlyeffect.impl.Predicated

import scala.language.implicitConversions
import scala.scalajs.js

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
    abstract class Prefixed(s: String) extends D[T](property) {
      override def apply(value: T): Declaration = Declaration(property, s"$s ${value.toString}")
    }
    val inherit         = Declaration(property, "inherit")
    val initial         = Declaration(property, "initial")
    val unset           = Declaration(property, "unset")
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
    val thin: Declaration   = apply("thin")
    val medium: Declaration = apply("medium")
    val thick: Declaration  = apply("thick")
  }

  trait Hidden extends DS {
    val hidden: Declaration = apply("hidden")
  }

  trait Visible extends DS {
    val visible: Declaration = apply("visible")
  }

  trait HiddenOrVisible extends Hidden with Visible

  trait LinStyle extends DS with None with Hidden {
    val dotted: Declaration = apply("dotted")
    val dashed: Declaration = apply("dashed")
    val solid: Declaration  = apply("solid")
    val double: Declaration = apply("double")
    val groove: Declaration = apply("groove")
    val ridge: Declaration  = apply("ridge")
    val inset: Declaration  = apply("inset")
    val outset: Declaration = apply("outset")
  }

  trait LeaderType extends DS {
    val dotted: Declaration = apply("dotted")
    val solid: Declaration  = apply("solid")
    val space: Declaration  = apply("space")
  }

  trait Leader extends DS with LeaderType {
    override def apply(value: String): Declaration = super.apply(s"leader($value)")
  }

  trait LengthPercentage extends Length with Percent

  trait Attachment extends DS {
    val scroll: Declaration = apply("scroll")
    val fixed: Declaration  = apply("fixed")
    val local: Declaration  = apply("local")
  }

  trait BlendMode extends DS with Normal {
    val multiply: Declaration   = apply("multiply")
    val screen: Declaration     = apply("screen")
    val overlay: Declaration    = apply("overlay")
    val darken: Declaration     = apply("darken")
    val lighten: Declaration    = apply("lighten")
    val colorBurn: Declaration  = apply("color-burn")
    val hardLight: Declaration  = apply("hard-light")
    val softLight: Declaration  = apply("soft-light")
    val difference: Declaration = apply("difference")
    val exclusion: Declaration  = apply("exclusion")
    val hue: Declaration        = apply("hue")
    val saturation: Declaration = apply("saturation")
    val color: Declaration      = apply("color")
    val luminosity: Declaration = apply("luminosity")
  }

  trait Box extends DS {
    val borderBox: Declaration  = apply("border-box")
    val paddingBox: Declaration = apply("padding-box")
    val contentBox: Declaration = apply("content-box")
  }

  trait FontWeightAbsolute extends DS with Number {
    val normal: Declaration = apply("normal")
    val bold: Declaration   = apply("bold")
  }

  trait AbsoluteSize extends DS {
    val xxSmall: Declaration = apply("xx-small")
    val xSmall: Declaration  = apply("x-small")
    val small: Declaration   = apply("small")
    val medium: Declaration  = apply("medium")
    val large: Declaration   = apply("large")
    val xLarge: Declaration  = apply("x-large")
    val xxLarge: Declaration = apply("xx-large")
  }

  trait RelativeSize extends DS {
    val smaller: Declaration = apply("smaller")
    val larger: Declaration  = apply("larger")
  }

  trait DisplayOutside extends DS {
    val block: Declaration  = apply("block")
    val inline: Declaration = apply("inline")
    val runIn: Declaration  = apply("run-in")
  }

  trait DisplayBox extends DS with None {
    val contents: Declaration = apply("contents")
  }

  trait DisplayInside extends DS {
    val flow: Declaration     = apply("flow")
    val flowRoot: Declaration = apply("flow-root")
    val table: Declaration    = apply("table")
    val flex: Declaration     = apply("flex")
    val grid: Declaration     = apply("grid")
    val ruby: Declaration     = apply("ruby")
  }

  trait DisplayInternal extends DS {
    val tableRowGroup: Declaration     = apply("table-row-group")
    val tableHeaderGroup: Declaration  = apply("table-header-group")
    val tableFooterGroup: Declaration  = apply("table-footer-group")
    val tableRow: Declaration          = apply("table-row")
    val tableCell: Declaration         = apply("table-cell")
    val tableColumnGroup: Declaration  = apply("table-column-group")
    val tableColumn: Declaration       = apply("table-column")
    val tableCaption: Declaration      = apply("table-caption")
    val rubyBase: Declaration          = apply("ruby-base")
    val rubyText: Declaration          = apply("ruby-text")
    val rubyBaseContainer: Declaration = apply("ruby-base-container")
    val rubyTextContainer: Declaration = apply("ruby-text-container")
  }

  trait DisplayLegacy extends DS {
    val inlineBlock: Declaration    = apply("inline-block")
    val inlineListItem: Declaration = apply("inline-list-item")
    val inlineTable: Declaration    = apply("inline-table")
    val inlineFlex: Declaration     = apply("inline-flex")
    val inlineGrid: Declaration     = apply("inline-grid")
  }

  trait DisplayListItem extends DS {
    val block_listItem: Declaration           = apply("block list-item")
    val block_flow_listItem: Declaration      = apply("block flow list-item")
    val block_flowRoot_listItem: Declaration  = apply("block flow-root list-item")
    val inline_listItem: Declaration          = apply("inline list-item")
    val inline_flow_listItem: Declaration     = apply("inline flow list-item")
    val inline_flowRoot_listItem: Declaration = apply("inline flow-root list-item")
    val runIn_listItem: Declaration           = apply("run-in list-item")
    val runIn_flow_listItem: Declaration      = apply("run-in flow list-item")
    val runIn_flowRoot_listItem: Declaration  = apply("run-in flow-root list-item")
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
    val revert: Declaration = apply("revert")
  }
  def alignContent: DS = Declaration("align-content")

  trait BaselinePosition extends DS {
    val firstBaseline: Declaration = apply("first baseline")
    val lastBaseline: Declaration  = apply("last baseline")
    val baseline: Declaration      = apply("baseline")
  }

  trait SelfPosition extends DS {
    val center: Declaration    = apply("center")
    val start: Declaration     = apply("start")
    val end: Declaration       = apply("end")
    val selfStart: Declaration = apply("self-start")
    val selfEnd: Declaration   = apply("self-end")
    val flexStart: Declaration = apply("flex-start")
    val flexEnd: Declaration   = apply("flex-end")
  }

  trait LeftOrRight extends DS {
    val left: Declaration  = apply("left")
    val right: Declaration = apply("right")
  }

  lazy val alignItems = new DeclarationConstructor[String]("align-items") with Normal with BaselinePosition
  with SelfPosition { self =>
    val stretch: Declaration = apply("stretch")
    val safe                 = new Prefixed("safe") with SelfPosition {}
    val unsafe               = new Prefixed("unsafe") with SelfPosition {}
  }

  def alignSelf: DS = Declaration("align-self")

  lazy val appearance = new DeclarationConstructor[String]("appearance") with None with Auto with Compat {
    val button: Declaration    = apply("button")
    val textfield: Declaration = apply("textfield")
  }
  lazy val backfaceVisibility             = new DeclarationConstructor[String]("backface-visibility") with HiddenOrVisible {}
  def background: DS                      = Declaration("background")
  lazy val backgroundAttachment           = new DeclarationConstructor[String]("background-attachment") with Attachment {}
  lazy val backgroundBlendMode            = new DeclarationConstructor[String]("background-blend-mode") with BlendMode {}
  lazy val backgroundClip                 = new DeclarationConstructor[String]("background-clip") with Box {}
  lazy val backgroundColor: DS with Color = new DeclarationConstructor[String]("background-color") with Color {}
  def backgroundImage: DS                 = Declaration("background-image")
  lazy val backgroundOrigin               = new DeclarationConstructor[String]("background-origin") with Box {}
  def backgroundPosition: DS              = Declaration("background-position")
  def backgroundRepeat: DS                = Declaration("background-repeat")
  def backgroundSize: DS                  = Declaration("background-size")
  lazy val blockSize                      = new DeclarationConstructor[String]("block-size") with Auto with Length {}
  def border: DS                          = Declaration("border")
  def borderBlock: DS                     = Declaration("border-block")

  trait LineStyle extends None {
    val hidden: Declaration = apply("hidden")
    val dotted: Declaration = apply("dotted")
    val dashed: Declaration = apply("dashed")
    val solid: Declaration  = apply("solid")
    val double: Declaration = apply("double")
    val groove: Declaration = apply("groove")
    val ridge: Declaration  = apply("ridge")
    val inset: Declaration  = apply("inset")
    val outset: Declaration = apply("outset")
  }
  lazy val borderStyle  = new DeclarationConstructor[String]("border-style") with LineStyle         {}
  lazy val borderColor  = new DeclarationConstructor[String]("border-color") with Color             {}
  lazy val borderRadius = new DeclarationConstructor[String]("border-radius") with LengthPercentage {}

  def boxShadow: DS = Declaration("box-shadow")

  def font: DS = Declaration("font")

  trait ContentDistribution extends DS {
    val spaceBetween: Declaration = apply("space-between")
    val spaceAround: Declaration  = apply("space-around")
    val spaceEvenly: Declaration  = apply("space-evenly")
    val stretch: Declaration      = apply("stretch")
  }
  lazy val justifyContent = new DeclarationConstructor[String]("justify-content") with Normal with ContentDistribution
  with LeftOrRight {
    val safe   = new Prefixed("safe") with ContentPosition   {}
    val unsafe = new Prefixed("unsafe") with ContentPosition {}
  }

  trait ContentPosition extends DS {
    val center: Declaration    = apply("center")
    val start: Declaration     = apply("start")
    val end: Declaration       = apply("end")
    val flexStart: Declaration = apply("flex-start")
    val flexEnd: Declaration   = apply("flex-end")
  }

  lazy val justifyItems = new DeclarationConstructor[String]("justify-items") with BaselinePosition with SelfPosition
  with Normal with Auto with LeftOrRight { self =>
    val stretch: Declaration = apply("stretch")
    val safe                 = new Prefixed("safe") with SelfPosition
    val unsafe               = new Prefixed("unsafe") with SelfPosition
  }

  lazy val lineHeight = new DeclarationConstructor[String]("line-height") with Normal with LengthPercentage {
    def number(d: Double): Declaration = apply(s"$d")
  }

  lazy val textAlign = new DeclarationConstructor[String]("text-align") {
    val start: Declaration       = apply("start")
    val send: Declaration        = apply("send")
    val left: Declaration        = apply("left")
    val right: Declaration       = apply("right")
    val center: Declaration      = apply("center")
    val justify: Declaration     = apply("justify")
    val matchParent: Declaration = apply("match-parent")
  }

  lazy val textRendering = new DeclarationConstructor[String]("text-rendering") with Auto {
    val optimizeSpeed: Declaration      = apply("optimizeSpeed")
    val optimizeLegibility: Declaration = apply("optimizeLegibility")
    val geometricPrecision: Declaration = apply("geometricPrecision")
  }

  def textShadow: DS = Declaration("text-shadow")

  //TODO: This is not a complete type
  lazy val height = new DeclarationConstructor[String]("height") with Auto with LengthPercentage {
    val available: Declaration  = apply("available")
    val minContent: Declaration = apply("min-content")
    val maxContent: Declaration = apply("max-content")
    val fitContent: Declaration = apply("fit-content")
  }
  //TODO: This is not a complete type
  lazy val width = new DeclarationConstructor[String]("width") with Auto with LengthPercentage {
    val available: Declaration  = apply("available")
    val minContent: Declaration = apply("min-content")
    val maxContent: Declaration = apply("max-content")
    val fitContent: Declaration = apply("fit-content")
  }

  trait MinMaxWidthHeight extends DS with LengthPercentage with None {
    //"<length> | <percentage> | none | max-content | min-content | fit-content | fill-available",
    val maxContent: Declaration    = apply("max-content")
    val minContent: Declaration    = apply("min-content")
    val fitContent: Declaration    = apply("fit-content")
    val fillAvailable: Declaration = apply("fill-available")
  }

  lazy val minWidth  = new DeclarationConstructor[String]("min-width") with MinMaxWidthHeight  {}
  lazy val maxWidth  = new DeclarationConstructor[String]("max-width") with MinMaxWidthHeight  {}
  lazy val minHeight = new DeclarationConstructor[String]("min-height") with MinMaxWidthHeight {}
  lazy val maxHeight = new DeclarationConstructor[String]("max-height") with MinMaxWidthHeight {}

  lazy val margin = new DeclarationConstructor[String]("margin") with LengthPercentage with Auto {

    def apply(vertical: String, horizontal: String): Declaration            = apply(s"$vertical $horizontal")
    def apply(top: String, horizontal: String, bottom: String): Declaration = apply(s"$top $horizontal $bottom")

    def apply(top: String, right: String, bottom: String, left: String): Declaration =
      apply(s"$top $right $bottom $left")
  }

  lazy val cursor = new DeclarationConstructor[String]("cursor") with Auto with None {
    val default: Declaration      = apply("default")
    val contextMenu: Declaration  = apply("context-menu")
    val help: Declaration         = apply("help")
    val pointer: Declaration      = apply("pointer")
    val progress: Declaration     = apply("progress")
    val waitCursor: Declaration   = apply("wait")
    val cell: Declaration         = apply("cell")
    val crosshair: Declaration    = apply("crosshair")
    val text: Declaration         = apply("text")
    val verticalText: Declaration = apply("vertical-text")
    val alias: Declaration        = apply("alias")
    val copy: Declaration         = apply("copy")
    val move: Declaration         = apply("move")
    val noDrop: Declaration       = apply("no-drop")
    val notAllowed: Declaration   = apply("not-allowed")
    val eResize: Declaration      = apply("e-resize")
    val nResize: Declaration      = apply("n-resize")
    val neResize: Declaration     = apply("ne-resize")
    val nwResize: Declaration     = apply("nw-resize")
    val sResize: Declaration      = apply("s-resize")
    val seResize: Declaration     = apply("se-resize")
    val swResize: Declaration     = apply("sw-resize")
    val wResize: Declaration      = apply("w-resize")
    val ewResize: Declaration     = apply("ew-resize")
    val nsResize: Declaration     = apply("ns-resize")
    val nsewResize: Declaration   = apply("nsew-resize")
    val nwseResize: Declaration   = apply("nwse-resize")
    val colResize: Declaration    = apply("col-resize")
    val rowResize: Declaration    = apply("row-resize")
    val allScroll: Declaration    = apply("all-scroll")
    val zoomIn: Declaration       = apply("zoom-in")
    val zoomOut: Declaration      = apply("zoom-out")
    val grab: Declaration         = apply("grab")
    val grabbing: Declaration     = apply("grabbing")
  }

  val padding: DS = Declaration("padding")

  lazy val paddingLeft   = new DeclarationConstructor[String]("padding-left") with LengthPercentage   {}
  lazy val paddingRight  = new DeclarationConstructor[String]("padding-right") with LengthPercentage  {}
  lazy val paddingTop    = new DeclarationConstructor[String]("padding-top") with LengthPercentage    {}
  lazy val paddingBottom = new DeclarationConstructor[String]("padding-bottom") with LengthPercentage {}

  lazy val position = new DeclarationConstructor[String]("position") {
    val static: Declaration   = apply("static")
    val relative: Declaration = apply("relative")
    val absolute: Declaration = apply("absolute")
    val sticky: Declaration   = apply("sticky")
    val fixed: Declaration    = apply("fixed")
  }

  val opacity: D[Double] = Declaration("opacity")

  lazy val display = new DeclarationConstructor[String]("display") with Display {}

  val flex: DS = Declaration("flex")

  def flexBasis: DS = Declaration("flex-basis")

  trait FlexDirection extends DS {
    val row: Declaration           = apply("row")
    val rowReverse: Declaration    = apply("row-reverse")
    val column: Declaration        = apply("column")
    val columnReverse: Declaration = apply("column-reverse")
  }

  trait FlexWrap extends DS {
    val nowrap: Declaration      = apply("nowrap")
    val wrap: Declaration        = apply("wrap")
    val wrapReverse: Declaration = apply("wrap-reverse")
  }

  lazy val flexDirection = new DeclarationConstructor[String]("flex-direction") with FlexDirection          {}
  lazy val flexFlow      = new DeclarationConstructor[String]("flex-flow") with FlexDirection with FlexWrap {}

  val flexGrow: D[Double] = Declaration("flex-grow")

  val flexShrink: D[Double] = Declaration("flex-shrink")

  lazy val flexWrap = new DeclarationConstructor[String]("flex-wrap") with FlexWrap {}

  lazy val float = new DeclarationConstructor[String]("float") with None {
    val left: Declaration        = apply("left")
    val right: Declaration       = apply("right")
    val inlineStart: Declaration = apply("inline-start")
    val inlineEnd: Declaration   = apply("inline-end")
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
    val ultraCondensed: Declaration = apply("ultra-condensed")
    val extraCondensed: Declaration = apply("extra-condensed")
    val condensed: Declaration      = apply("condensed")
    val semCondensed: Declaration   = apply("sem-condensed")
    val semiExpanded: Declaration   = apply("semi-expanded")
    val expanded: Declaration       = apply("expanded")
    val extraExpanded: Declaration  = apply("extra-expanded")
    val ultraExpanded: Declaration  = apply("ultra-expanded")

  }
  lazy val fontStretch = new DeclarationConstructor[String]("font-stretch") with FontStretchAbsolute {}

  lazy val fontStyle = new DeclarationConstructor[String]("font-style") with Normal {
    val italic: Declaration = apply("italic")
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
    val weight: Declaration = apply("weight")
    val style: Declaration  = apply("style")
  }

  //TODO: this is incomplete - I think I could do this one
  lazy val fontVariant = new DeclarationConstructor[String]("font-variant") with Normal with None {}

  //TODO: this is incomplete - I think I could do this one
  lazy val fontVariantAlternatives = new DeclarationConstructor[String]("font-variant-alternatives") with Normal
  with None {}

  lazy val fontVariantCaps = new DeclarationConstructor[String]("font-variant-caps") with Normal {
    val smallCaps: Declaration     = apply("small-caps")
    val allSmallCaps: Declaration  = apply("all-small-caps")
    val petiteCaps: Declaration    = apply("petite-caps")
    val allPetiteCaps: Declaration = apply("all-petite-caps")
    val unicase: Declaration       = apply("unicase")
    val titlingCaps: Declaration   = apply("titling-caps")
  }

  lazy val fontVariantPosition = new DeclarationConstructor[String]("font-variant-position") with Normal {
    val sub: Declaration     = apply("sub")
    val `super`: Declaration = apply("super")
  }

  lazy val fontWeight = new DeclarationConstructor[String]("font-weight") with FontWeightAbsolute {
    val bolder: Declaration  = apply("bolder")
    val lighter: Declaration = apply("lighter")
  }

  lazy val marginLeft   = new DeclarationConstructor[String]("margin-left") with Auto with LengthPercentage   {}
  lazy val marginRight  = new DeclarationConstructor[String]("margin-right") with Auto with LengthPercentage  {}
  lazy val marginTop    = new DeclarationConstructor[String]("margin-top") with Auto with LengthPercentage    {}
  lazy val marginBottom = new DeclarationConstructor[String]("margin-bottom") with Auto with LengthPercentage {}

  lazy val textOverflow = new DeclarationConstructor[String]("text-overflow") {
    val clip: Declaration     = apply("clip")
    val ellipses: Declaration = apply("ellipses")
  }

  lazy val overflow = new DeclarationConstructor[String]("overflow") with Auto {
    val visible: Declaration = apply("visible")
    val hidden: Declaration  = apply("hidden")
    val clip: Declaration    = apply("clip")
    val scroll: Declaration  = apply("scroll")
  }

  trait RightLeftTopBottom extends LengthPercentage with Auto

  lazy val top    = new DeclarationConstructor[String]("top") with RightLeftTopBottom    {}
  lazy val bottom = new DeclarationConstructor[String]("bottom") with RightLeftTopBottom {}
  lazy val left   = new DeclarationConstructor[String]("left") with RightLeftTopBottom   {}
  lazy val right  = new DeclarationConstructor[String]("right") with RightLeftTopBottom  {}

  def rgb(r: Int, g: Int, b: Int): String             = s"rgb($r,$g,$b)"
  def rgba(r: Int, g: Int, b: Int, a: Double): String = s"rgb($r,$g,$b,$a)"

}
