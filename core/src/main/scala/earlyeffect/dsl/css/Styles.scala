package earlyeffect.dsl.css

import earlyeffect.Declaration

import scala.scalajs.js

object Styles {

  def selector[T <: DeclarationOrSelector](selector: String)(members: T*) = Selector(selector, members: _*)

  trait DeclarationOrSelector {

    def mkString(
        className: String,
        keyFrames: js.Array[KeyFrames] = js.Array(),
        mediaQueries: js.Array[MediaQuery] = js.Array()
    ): String

    override def toString: String = mkString("")
  }

  case class KeyFrames(name: String, selectors: KeyframeSelector*) extends DeclarationOrSelector {
    override def mkString(
        className: String,
        keyFrames: js.Array[KeyFrames],
        mediaQueries: js.Array[MediaQuery]
    ): String = {
      keyFrames.push(this)
      s"animation-name: $className-$name;"
    }
  }
  case class MediaQuery(query: String, declaration: DeclarationOrSelector*) extends DeclarationOrSelector {
    override def mkString(
        className: String,
        keyFrames: js.Array[KeyFrames],
        mediaQueries: js.Array[MediaQuery]
    ): String = {
      mediaQueries.push(this)
      ""
    }

    def render: String =
      s"@media $query {\n" + declaration
        .map { x =>
          x.mkString("")
        }
        .mkString("\n") + "}"
  }

  sealed abstract class DeclarationConstructor[T](property: String) {
    abstract class Prefixed(s: String) extends D[T](property) {
      override def apply(value: T): Declaration = Declaration(property, s"$s ${value.toString}")
    }
    abstract class Suffixed(s: String) extends D[T](property) {
      override def apply(value: T): Declaration = Declaration(property, s"${value.toString} $s")
    }
    def inherit         = Declaration(property, "inherit")
    def initial         = Declaration(property, "initial")
    def unset           = Declaration(property, "unset")
    def apply(value: T) = Declaration(property, value.toString)
  }

  private[earlyeffect] case class SimpleConstructor[T](property: String) extends DeclarationConstructor[T](property)

  type D[T] = DeclarationConstructor[T]
  type DS   = D[String]

  def apply[T](name: String): D[T] = SimpleConstructor[T](name)

  def apply(name: String, value: String) = Declaration(name, value)

  def apply[T](name: String, value: T): Declaration = Declaration(name, value.toString)

  case class KeyframeSelector(selector: String, members: Declaration*) extends DeclarationOrSelector {
    override def mkString(
        className: String,
        keyFrames: js.Array[KeyFrames],
        mediaQueries: js.Array[MediaQuery]
    ): String =
      s"$selector {\n${members.map("    " + _.mkString(className, keyFrames, mediaQueries)).mkString("\n")}\n  }"
  }

  class Selector private (val selector: String, val members: Seq[DeclarationOrSelector]) extends DeclarationOrSelector {

    override def mkString(className: String, ks: js.Array[KeyFrames], ms: js.Array[MediaQuery]): String = {
      val ss = members.collect { case s: Selector => s }.map(s => s.mkString(className, ks)).mkString("\n")
      val inner = members
        .collect { case p @ (_: Declaration | _: KeyFrames | _: KeyframeSelector | _: MediaQuery) => p }
        .map(_.mkString(className, ks, ms))
        .filter(_.nonEmpty)
        .map("  " + _)
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
    val cap: ST = suffixed("cap")
    val ch: ST  = suffixed("ch")
    val em: ST  = suffixed("em")
    val ex: ST  = suffixed("ex")
    val ic: ST  = suffixed("ic")
    val lh: ST  = suffixed("lh")
    val rem: ST = suffixed("rem")
    val rlh: ST = suffixed("rlh")
  }

  trait ViewportPercentageLength extends Suffixed[Double] {
    val vh: ST   = suffixed("vh")
    val vw: ST   = suffixed("vw")
    val vi: ST   = suffixed("vi")
    val vb: ST   = suffixed("vb")
    val vmin: ST = suffixed("vmin")
    val vmax: ST = suffixed("vmax")
  }

  trait AbsoluteLength extends Suffixed[Double] {
    val zero   = apply("0")
    val px: ST = suffixed("px")
    val cm: ST = suffixed("cm")
    val mm: ST = suffixed("mm")
    val q: ST  = suffixed("Q")
    val in: ST = suffixed("in")
    val pc: ST = suffixed("pc")
    val pt: ST = suffixed("pt")
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

  trait BaselinePosition extends DS {
    def firstBaseline: Declaration = apply("first baseline")
    def lastBaseline: Declaration  = apply("last baseline")
    def baseline: Declaration      = apply("baseline")
  }

  trait SelfPosition extends DS {
    def center: Declaration    = apply("center")
    def start: Declaration     = apply("start")
    def end: Declaration       = apply("end")
    def selfStart: Declaration = apply("self-start")
    def selfEnd: Declaration   = apply("self-end")
    def flexStart: Declaration = apply("flex-start")
    def flexEnd: Declaration   = apply("flex-end")
  }

  trait LeftOrRight extends DS {
    def left: Declaration  = apply("left")
    def right: Declaration = apply("right")
  }

  trait LineStyle extends None {
    def hidden: Declaration = apply("hidden")
    def dotted: Declaration = apply("dotted")
    def dashed: Declaration = apply("dashed")
    def solid: Declaration  = apply("solid")
    def double: Declaration = apply("double")
    def groove: Declaration = apply("groove")
    def ridge: Declaration  = apply("ridge")
    def inset: Declaration  = apply("inset")
    def outset: Declaration = apply("outset")
  }

  trait ContentDistribution extends DS {
    def spaceBetween: Declaration = apply("space-between")
    def spaceAround: Declaration  = apply("space-around")
    def spaceEvenly: Declaration  = apply("space-evenly")
    def stretch: Declaration      = apply("stretch")
  }

  trait ContentPosition extends DS {
    def center: Declaration    = apply("center")
    def start: Declaration     = apply("start")
    def end: Declaration       = apply("end")
    def flexStart: Declaration = apply("flex-start")
    def flexEnd: Declaration   = apply("flex-end")
  }

  trait MinMaxWidthHeight extends DS with LengthPercentage with None {
    //"<length> | <percentage> | none | max-content | min-content | fit-content | fill-available",
    def maxContent: Declaration    = apply("max-content")
    def minContent: Declaration    = apply("min-content")
    def fitContent: Declaration    = apply("fit-content")
    def fillAvailable: Declaration = apply("fill-available")
  }

  trait OverFlow extends DS with Auto {
    def visible: Declaration = apply("visible")
    def hidden: Declaration  = apply("hidden")
    def clip: Declaration    = apply("clip")
    def scroll: Declaration  = apply("scroll")
  }

  object all extends DS(property = "all") {
    def revert: Declaration = apply("revert")
  }
  object alignContent extends DS("align-content")

  object alignItems
      extends DeclarationConstructor[String]("align-items")
      with Normal
      with BaselinePosition
      with SelfPosition {
    def stretch: Declaration = apply("stretch")
    object safe   extends Prefixed("safe") with SelfPosition   {}
    object unsafe extends Prefixed("unsafe") with SelfPosition {}
  }

  object alignSelf extends DS("align-self")

  object animation               extends DS("animation")
  object animationDelay          extends DS("animation-delay") with Time {}
  object animationDirection      extends DS("animation-direction")
  object animationDuration       extends DS("animation-duration") with Time {}
  object animationFillMode       extends DS("animation-fill-mode")
  object animationIterationCount extends DS("animation-iteration-count")
  object animationName           extends DS("animation-name") with None {}
  object animationPlayState      extends DS("animation-play-state")
  object animationTimingFunction extends DS("animation-timing-function")

  object appearance extends DS("appearance") with None with Auto with Compat {
    def button: Declaration    = apply("button")
    def textfield: Declaration = apply("textfield")
  }

  object backfaceVisibility   extends DS("backface-visibility") with HiddenOrVisible {}
  object background           extends DS("background")
  object backgroundAttachment extends DS("background-attachment") with Attachment {}
  object backgroundBlendMode  extends DS("background-blend-mode") with BlendMode {}
  object backgroundClip       extends DS("background-clip") with Box {}
  object backgroundColor      extends DS("background-color") with Color {}
  object backgroundImage      extends DS("background-image")
  object backgroundOrigin     extends DS("background-origin") with Box {}
  object backgroundPosition   extends DS("background-position")
  object backgroundRepeat     extends DS("background-repeat")
  object backgroundSize       extends DS("background-size")
  object blockSize            extends DS("block-size") with Auto with Length {}
  object border               extends DS("border")
  // Todo: not complete
  object borderWidth extends DS(property = "border-width") with LineWidth

  object borderBlock extends DS("border-block")

  object color extends DS(property = "color") with Color

  object zIndex extends D[Int](property = "z-index")

  object borderStyle extends DS("border-style") with LineStyle {}

  object borderColor extends DS("border-color") with Color {
    type C = values.Color
    def apply(c1: C): Declaration                                          = apply(c1.value)
    def apply(c1: String, c2: String, c3: String, c4: String): Declaration = apply(s"$c1 $c2 $c3 $c4")
    def apply(c1: C, c2: C, c3: C, c4: C): Declaration                     = apply(s"$c1 $c2 $c3 $c4")
  }

  object borderRadius extends DS("border-radius") with LengthPercentage {}

  object boxShadow extends DS("box-shadow")

  object font extends DS("font")

  object justifyContent
      extends DS("justify-content")
      with Normal
      with ContentDistribution
      with LeftOrRight
      with ContentPosition {
    object safe   extends Prefixed("safe") with ContentPosition   {}
    object unsafe extends Prefixed("unsafe") with ContentPosition {}
  }

  object justifyItems
      extends DS("justify-items")
      with BaselinePosition
      with SelfPosition
      with Normal
      with Auto
      with LeftOrRight { self =>
    def stretch: Declaration = apply("stretch")
    object safe   extends Prefixed("safe") with SelfPosition
    object unsafe extends Prefixed("unsafe") with SelfPosition
  }

  object lineHeight extends DS("line-height") with Normal with LengthPercentage {
    def number(d: Double): Declaration = apply(s"$d")
  }

  object textAlign extends DS("text-align") {
    def start: Declaration       = apply("start")
    def send: Declaration        = apply("send")
    def left: Declaration        = apply("left")
    def right: Declaration       = apply("right")
    def center: Declaration      = apply("center")
    def justify: Declaration     = apply("justify")
    def matchParent: Declaration = apply("match-parent")
  }

  object textRendering extends DS("text-rendering") with Auto {
    def optimizeSpeed: Declaration      = apply("optimizeSpeed")
    def optimizeLegibility: Declaration = apply("optimizeLegibility")
    def geometricPrecision: Declaration = apply("geometricPrecision")
  }

  object textShadow extends DS("text-shadow")

  //TODO: This is not a complete type
  object height extends DS("height") with Auto with LengthPercentage {
    def available: Declaration  = apply("available")
    def minContent: Declaration = apply("min-content")
    def maxContent: Declaration = apply("max-content")
    def fitContent: Declaration = apply("fit-content")
  }

  //TODO: This is not a complete type
  object width extends DS("width") with Auto with LengthPercentage {
    def available: Declaration  = apply("available")
    def minContent: Declaration = apply("min-content")
    def maxContent: Declaration = apply("max-content")
    def fitContent: Declaration = apply("fit-content")
  }

  object minWidth  extends DS("min-width") with MinMaxWidthHeight  {}
  object maxWidth  extends DS("max-width") with MinMaxWidthHeight  {}
  object minHeight extends DS("min-height") with MinMaxWidthHeight {}
  object maxHeight extends DS("max-height") with MinMaxWidthHeight {}

  object margin extends DS("margin") with LengthPercentage with Auto {

    def apply(vertical: String, horizontal: String): Declaration            = apply(s"$vertical $horizontal")
    def apply(top: String, horizontal: String, bottom: String): Declaration = apply(s"$top $horizontal $bottom")

    def apply(top: String, right: String, bottom: String, left: String): Declaration =
      apply(s"$top $right $bottom $left")
  }

  object cursor extends DS("cursor") with Auto with None {
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

  object padding extends DS("padding") with LengthPercentage {}

  object paddingLeft   extends DS("padding-left") with LengthPercentage   {}
  object paddingRight  extends DS("padding-right") with LengthPercentage  {}
  object paddingTop    extends DS("padding-top") with LengthPercentage    {}
  object paddingBottom extends DS("padding-bottom") with LengthPercentage {}

  object position extends DS("position") {
    def static: Declaration   = apply("static")
    def relative: Declaration = apply("relative")
    def absolute: Declaration = apply("absolute")
    def sticky: Declaration   = apply("sticky")
    def fixed: Declaration    = apply("fixed")
  }

  object opacity extends D[Double]("opacity")

  object display extends DS("display") with Display {}

  object flex extends DS("flex")

  object flexBasis extends DS("flex-basis")

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

  object flexDirection extends DS("flex-direction") with FlexDirection          {}
  object flexFlow      extends DS("flex-flow") with FlexDirection with FlexWrap {}

  object flexGrow extends D[Double]("flex-grow")

  object flexShrink extends D[Double]("flex-shrink")

  object flexWrap extends DS("flex-wrap") with FlexWrap {}

  object float extends DS("float") with None {
    def left: Declaration        = apply("left")
    def right: Declaration       = apply("right")
    def inlineStart: Declaration = apply("inline-start")
    def inlineEnd: Declaration   = apply("inline-end")
  }

  object fontFamily extends DS("font-family")

  object fontFeatureSettings extends DS("font-feature-settings")

  object fontKerning extends DS("font-kerning") with Auto with Normal with None {}

  object fontLanguageOverride extends DS("font-language-override") with Normal {}

  object fontOpticalSizing extends DS("font-optical-sizing") with Auto with None {}

  object fontSize extends DS("font-size") with AbsoluteSize with RelativeSize with LengthPercentage {}

  object fontSizeAdjust extends DS("font-size-adjust") with None {
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
  object fontStretch extends DS("font-stretch") with FontStretchAbsolute {}

  object fontStyle extends DS("font-style") with Normal {
    def italic: Declaration = apply("italic")

    object oblique {
      private def property(d: Double, suffix: String) = apply(s"oblique $d$suffix")
      def deg(d: Double): Declaration                 = property(d, "deg")
      def rad(d: Double): Declaration                 = property(d, "rad")
      def grad(d: Double): Declaration                = property(d, "grad")
      def turn(d: Double): Declaration                = property(d, "turn")
    }
  }

  object fontSynthesis extends DS("font-synthesis") with None {
    def weight: Declaration = apply("weight")
    def style: Declaration  = apply("style")
  }

  //TODO: this is incomplete - I think I could do this one
  object fontVariant extends DS("font-variant") with Normal with None {}

  //TODO: this is incomplete - I think I could do this one
  object fontVariantAlternatives extends DS("font-variant-alternatives") with Normal with None {}

  object fontVariantCaps extends DS("font-variant-caps") with Normal {
    def smallCaps: Declaration     = apply("small-caps")
    def allSmallCaps: Declaration  = apply("all-small-caps")
    def petiteCaps: Declaration    = apply("petite-caps")
    def allPetiteCaps: Declaration = apply("all-petite-caps")
    def unicase: Declaration       = apply("unicase")
    def titlingCaps: Declaration   = apply("titling-caps")
  }

  object fontVariantPosition extends DS("font-variant-position") with Normal {
    def sub: Declaration     = apply("sub")
    def `super`: Declaration = apply("super")
  }

  object fontWeight extends DS("font-weight") with FontWeightAbsolute {
    def bolder: Declaration  = apply("bolder")
    def lighter: Declaration = apply("lighter")
  }

  object marginLeft   extends DS("margin-left") with Auto with LengthPercentage   {}
  object marginRight  extends DS("margin-right") with Auto with LengthPercentage  {}
  object marginTop    extends DS("margin-top") with Auto with LengthPercentage    {}
  object marginBottom extends DS("margin-bottom") with Auto with LengthPercentage {}

  object textOverflow extends DS("text-overflow") {
    def clip: Declaration     = apply("clip")
    def ellipsis: Declaration = apply("ellipsis")
  }

  object overflow extends DS("overflow") with Auto {
    def visible: Declaration = apply("visible")
    def hidden: Declaration  = apply("hidden")
    def clip: Declaration    = apply("clip")
    def scroll: Declaration  = apply("scroll")
  }

  trait RightLeftTopBottom extends LengthPercentage with Auto

  object top    extends DS("top") with RightLeftTopBottom    {}
  object bottom extends DS("bottom") with RightLeftTopBottom {}
  object left   extends DS("left") with RightLeftTopBottom   {}
  object right  extends DS("right") with RightLeftTopBottom  {}

  object transition extends DS(property = "transition")
  object transform  extends DS(property = "transform")

  object textTransform extends DS(property = "text-transform") with None {
    val capitalize: Declaration   = apply("capitalize")
    val uppercase: Declaration    = apply("uppercase")
    val lowercase: Declaration    = apply("lowercase")
    val fullWidth: Declaration    = apply("full-width")
    val fullSizeKana: Declaration = apply("full-size-kana")
  }

  object borderTop    extends DS(property = "border-top")
  object borderBottom extends DS(property = "border-bottom")
  object borderLeft   extends DS(property = "border-left")
  object borderRight  extends DS(property = "border-right")

  object textDecoration extends DS(property = "text-decoration") with None

  object overflowY extends DS(property = "overflow-y") with OverFlow
  object overflowX extends DS(property = "overflow-x") with OverFlow

  object listStyle     extends DS(property = "list-style") with None
  object listStyleType extends DS(property = "list-style-type") with None

  object listStylePosition extends DS(property = "list-style-position") {
    def inside: Declaration  = apply("inside")
    def outside: Declaration = apply("outside")
  }
  //Todo: need to create URL type
  object listStyleImage extends DS(property = "list-style-image") with None

  object whiteSpace extends DS(property = "white-space") {
    def normal: Declaration  = apply("normal")
    def pre: Declaration     = apply("pre")
    def nowrap: Declaration  = apply("nowrap")
    def preWrap: Declaration = apply("pre-wrap")
    def preLine: Declaration = apply("pre-line")
  }

  object userSelect extends DS(property = "user-select") with None with Auto {
    def all: Declaration     = apply("all")
    def contain: Declaration = apply("contain")
    def text: Declaration    = apply("text")
  }

  object webkitTouchCallout extends DS(property = "-webkit-touch-callout") with None {
    def default: Declaration = apply("default")
  }

  object webkitOverflowScrolling extends DS(property = "-webkit-overflow-scrolling") with Auto {
    def touch: Declaration = apply("touch")
  }

  object grid            extends DS(property = "grid")
  object gridArea        extends DS(property = "grid-area")
  object gridAutoColumns extends DS(property = "grid-auto-columns")

  object gridAutoFlow extends DS(property = "grid-auto-flow") {
    def row: Declaration         = apply("row")
    def column: Declaration      = apply("column")
    def rowDense: Declaration    = apply("row dense")
    def columnDense: Declaration = apply("column dense")
  }

  object gridAutoRows        extends DS(property = "grid-auto-rows")
  object gridColumn          extends DS(property = "grid-column")
  object gridColumnEnd       extends DS(property = "grid-column-end")
  object gridColumGap        extends DS(property = "grid-colum-gap")
  object gridColumnStart     extends DS(property = "grid-column-start")
  object gridGap             extends DS(property = "grid-gap")
  object gridRow             extends DS(property = "grid-row")
  object gridRowEnd          extends DS(property = "grid-row-end")
  object gridRowGrap         extends DS(property = "grid-row-grap")
  object gridRowStart        extends DS(property = "grid-row-start")
  object gridTemplate        extends DS(property = "grid-template") with None
  object gridTemplateAreas   extends DS(property = "grid-template-areas")
  object gridTemplateColumns extends DS(property = "grid-template-columns") with None
  object gridTemplateRows    extends DS(property = "grid-template-rows") with None

  object pointerEvents extends DS(property = "pointer-events") with Auto with None {
    def visiblePainted: Declaration = apply("visiblePainted")
    def visibleFill: Declaration    = apply("visibleFill")
    def visibleStroke: Declaration  = apply("visibleStroke")
    def visible: Declaration        = apply("visible")
    def painted: Declaration        = apply("painted")
  }

  object verticalAlign extends DS(property = "vertical-align") with LengthPercentage {
    def baseline: Declaration   = apply("baseline")
    def sub: Declaration        = apply("sub")
    def `super`: Declaration    = apply("super")
    def textTop: Declaration    = apply("text-top")
    def textBottom: Declaration = apply("text-bottom")
    def middle: Declaration     = apply("middle")
    def top: Declaration        = apply("top")
    def bottom: Declaration     = apply("bottom")
  }

  object resize extends DS(property = "resize") with None {
    def both: Declaration       = apply("both")
    def horizontal: Declaration = apply("horizontal")
    def vertical: Declaration   = apply("vertical")
    def block: Declaration      = apply("block")
    def inline: Declaration     = apply("inline")
  }

  object letterSpacing extends DS(property = "letter-spacing") with Length {
    def normal: Declaration = apply("normal")
  }

  //Todo: not complete... but should capture most use cases
  object touchAction extends DS(property = "touch-action") with Auto with None {
    def panX: Declaration         = apply("pan-x")
    def panY: Declaration         = apply("pan-y")
    def panLeft: Declaration      = apply("pan-left")
    def panRight: Declaration     = apply("pan-right")
    def panUp: Declaration        = apply("pan-up")
    def panDown: Declaration      = apply("pan-down")
    def pinchZoom: Declaration    = apply("pinch-zoom")
    def manipulation: Declaration = apply("manipulation")
    def panX_panY: Declaration    = apply("pan-x pan-y")
  }

  //Todo: this should be doable as an apply normal or two LengthPercentages
  object gap extends DS(property = "gap") {
    def zero: Declaration = apply("0")
  }

  object columnGap extends DS(property = "column-gap") with Normal with LengthPercentage
  object rowGap    extends DS(property = "row-gap") with Normal with LengthPercentage

  object outline extends DS(property = "outline")

  //Todo: not complete
  object content extends DS(property = "content") with Normal with None

  //Todo: not complete
  object textIndent extends DS(property = "text-indent") with LengthPercentage

//  def rgb(r: Int, g: Int, b: Int): String             = s"rgb($r,$g,$b)"
//  def rgba(r: Int, g: Int, b: Int, a: Double): String = s"rgb($r,$g,$b,$a)"

}
