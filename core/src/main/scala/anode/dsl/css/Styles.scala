package anode.dsl.css

import anode.Declaration
import anode.dsl.css.ds._

import scala.scalajs.js

object Styles {

  def selector[T <: DeclarationOrSelector](selector: String)(members: T*) = Selector(selector, members: _*)

  trait DeclarationOrSelector {

    def mkString(
        className: String,
        keyFrames: js.Array[KeyFrames] = js.Array(),
        mediaQueries: js.Array[MediaQuery] = js.Array(),
    ): String

    override def toString: String = mkString("")
  }

  case class KeyFrames(name: String, selectors: KeyframeSelector*) extends DeclarationOrSelector {

    override def mkString(
        className: String,
        keyFrames: js.Array[KeyFrames],
        mediaQueries: js.Array[MediaQuery],
    ): String = {
      keyFrames.push(this)
      s"animation-name: $className-$name;"
    }
  }

  case class MediaQuery(query: String, declaration: DeclarationOrSelector*) extends DeclarationOrSelector {

    override def mkString(
        className: String,
        keyFrames: js.Array[KeyFrames],
        mediaQueries: js.Array[MediaQuery],
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

  abstract class DeclarationConstructor[T](property: String) {

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

    def customProperty(name: String, fallback: String = "") = {
      val n = if (name.startsWith("--")) name else s"--$name"
      Declaration(property, s"var($n${if (fallback.nonEmpty) s", $fallback" else ""})")
    }
  }

  private[anode] case class SimpleConstructor[T](property: String) extends DeclarationConstructor[T](property)

  def apply[T](name: String): D[T] = SimpleConstructor[T](name)

  def apply(name: String, value: String) = Declaration(name, value)

  def apply[T](name: String, value: T): Declaration = Declaration(name, value.toString)

  case class KeyframeSelector(selector: String, members: Declaration*) extends DeclarationOrSelector {

    override def mkString(
        className: String,
        keyFrames: js.Array[KeyFrames],
        mediaQueries: js.Array[MediaQuery],
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
      new Selector(
        s + selector,
        members.map {
          case x: Selector => x.prependAll(s)
          case y           => y
        },
      )
  }

  object Selector {

    def apply(selector: String, members: DeclarationOrSelector*): Selector =
      new Selector(
        selector,
        members.map {
          case x: Selector => x.prependAll(selector)
          case x           => x
        },
      )

    protected class SelectorConstructor(selector: String) {
      def apply(members: DeclarationOrSelector*): Selector = Selector.apply(selector, members: _*)
    }

    def apply(selector: String) = new SelectorConstructor(selector)
  }

  object all extends DS(property = "all") {
    def revert: Declaration = this.apply("revert")
  }
  object alignContent extends DS("align-content")

  object alignItems
      extends DeclarationConstructor[String]("align-items")
      with Normal
      with BaselinePosition
      with SelfPosition {
    def stretch: Declaration = this.apply("stretch")
    object safe   extends Prefixed("safe") with SelfPosition   {}
    object unsafe extends Prefixed("unsafe") with SelfPosition {}
  }

  object alignSelf extends DS("align-self")

  object animation               extends DS("animation")
  object animationDelay          extends DS("animation-delay") with Time    {}
  object animationDirection      extends DS("animation-direction")
  object animationDuration       extends DS("animation-duration") with Time {}
  object animationFillMode       extends DS("animation-fill-mode")
  object animationIterationCount extends DS("animation-iteration-count")
  object animationName           extends DS("animation-name") with None     {}
  object animationPlayState      extends DS("animation-play-state")
  object animationTimingFunction extends DS("animation-timing-function")

  object appearance extends DS("appearance") with None with Auto with Compat {
    def button: Declaration    = this.apply("button")
    def textfield: Declaration = this.apply("textfield")
  }

  object backfaceVisibility   extends DS("backface-visibility") with HiddenOrVisible {}
  object background           extends DS("background") with None with Color          {}
  object backgroundAttachment extends DS("background-attachment") with Attachment    {}
  object backgroundBlendMode  extends DS("background-blend-mode") with BlendMode     {}
  object backgroundClip       extends DS("background-clip") with Box                 {}
  object backgroundColor      extends DS("background-color") with Color              {}
  object backgroundImage      extends DS("background-image")
  object backgroundOrigin     extends DS("background-origin") with Box               {}
  object backgroundPosition   extends DS("background-position")
  object backgroundRepeat     extends DS("background-repeat")
  object backgroundSize       extends DS("background-size")
  object blockSize            extends DS("block-size") with Auto with Length         {}
  object border               extends DS("border") with LineWidth with None
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
  object boxSizing extends DS("box-sizing") with Box

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
    def stretch: Declaration = this("stretch")
    object safe   extends Prefixed("safe") with SelfPosition
    object unsafe extends Prefixed("unsafe") with SelfPosition
  }

  object lineHeight extends DS("line-height") with Normal with LengthPercentage {
    def number(d: Double): Declaration = this(s"$d")
  }

  object textAlign extends DS("text-align") {
    def start: Declaration       = this("start")
    def send: Declaration        = this("send")
    def left: Declaration        = this("left")
    def right: Declaration       = this("right")
    def center: Declaration      = this("center")
    def justify: Declaration     = this("justify")
    def matchParent: Declaration = this("match-parent")
  }

  object textRendering extends DS("text-rendering") with Auto {
    def optimizeSpeed: Declaration      = this("optimizeSpeed")
    def optimizeLegibility: Declaration = this("optimizeLegibility")
    def geometricPrecision: Declaration = this("geometricPrecision")
  }

  object textShadow extends DS("text-shadow")

  object Visibility extends DS("visibility") with HiddenOrVisible

  //TODO: This is not a complete type
  object height extends DS("height") with Auto with LengthPercentage {
    def available: Declaration  = this("available")
    def minContent: Declaration = this("min-content")
    def maxContent: Declaration = this("max-content")
    def fitContent: Declaration = this("fit-content")
  }

  //TODO: This is not a complete type
  object width extends DS("width") with Auto with LengthPercentage {
    def available: Declaration  = this("available")
    def minContent: Declaration = this("min-content")
    def maxContent: Declaration = this("max-content")
    def fitContent: Declaration = this("fit-content")
  }

  object minWidth  extends DS("min-width") with MinMaxWidthHeight  {}
  object maxWidth  extends DS("max-width") with MinMaxWidthHeight  {}
  object minHeight extends DS("min-height") with MinMaxWidthHeight {}
  object maxHeight extends DS("max-height") with MinMaxWidthHeight {}

  object margin extends DS("margin") with LengthPercentage with Auto {

    def apply(vertical: String, horizontal: String): Declaration            = this(s"$vertical $horizontal")
    def apply(top: String, horizontal: String, bottom: String): Declaration = this(s"$top $horizontal $bottom")

    def apply(top: String, right: String, bottom: String, left: String): Declaration =
      this(s"$top $right $bottom $left")
  }

  object cursor extends DS("cursor") with Auto with None {
    def default: Declaration      = this("default")
    def contextMenu: Declaration  = this("context-menu")
    def help: Declaration         = this("help")
    def pointer: Declaration      = this("pointer")
    def progress: Declaration     = this("progress")
    def waitCursor: Declaration   = this("wait")
    def cell: Declaration         = this("cell")
    def crosshair: Declaration    = this("crosshair")
    def text: Declaration         = this("text")
    def verticalText: Declaration = this("vertical-text")
    def alias: Declaration        = this("alias")
    def copy: Declaration         = this("copy")
    def move: Declaration         = this("move")
    def noDrop: Declaration       = this("no-drop")
    def notAllowed: Declaration   = this("not-allowed")
    def eResize: Declaration      = this("e-resize")
    def nResize: Declaration      = this("n-resize")
    def neResize: Declaration     = this("ne-resize")
    def nwResize: Declaration     = this("nw-resize")
    def sResize: Declaration      = this("s-resize")
    def seResize: Declaration     = this("se-resize")
    def swResize: Declaration     = this("sw-resize")
    def wResize: Declaration      = this("w-resize")
    def ewResize: Declaration     = this("ew-resize")
    def nsResize: Declaration     = this("ns-resize")
    def nsewResize: Declaration   = this("nsew-resize")
    def nwseResize: Declaration   = this("nwse-resize")
    def colResize: Declaration    = this("col-resize")
    def rowResize: Declaration    = this("row-resize")
    def allScroll: Declaration    = this("all-scroll")
    def zoomIn: Declaration       = this("zoom-in")
    def zoomOut: Declaration      = this("zoom-out")
    def grab: Declaration         = this("grab")
    def grabbing: Declaration     = this("grabbing")
  }

  object padding extends DS("padding") with LengthPercentage {}

  object paddingLeft   extends DS("padding-left") with LengthPercentage   {}
  object paddingRight  extends DS("padding-right") with LengthPercentage  {}
  object paddingTop    extends DS("padding-top") with LengthPercentage    {}
  object paddingBottom extends DS("padding-bottom") with LengthPercentage {}

  object position extends DS("position") {
    def static: Declaration   = this("static")
    def relative: Declaration = this("relative")
    def absolute: Declaration = this("absolute")
    def sticky: Declaration   = this("sticky")
    def fixed: Declaration    = this("fixed")
  }

  object opacity extends D[Double]("opacity")

  object display extends DS("display") with Display {}

  object flex extends DS("flex")

  object flexBasis extends DS("flex-basis")

  object flexDirection extends DS("flex-direction") with FlexDirection          {}
  object flexFlow      extends DS("flex-flow") with FlexDirection with FlexWrap {}

  object flexGrow extends D[Double]("flex-grow")

  object flexShrink extends D[Double]("flex-shrink")

  object flexWrap extends DS("flex-wrap") with FlexWrap {}

  object float extends DS("float") with None {
    def left: Declaration        = this("left")
    def right: Declaration       = this("right")
    def inlineStart: Declaration = this("inline-start")
    def inlineEnd: Declaration   = this("inline-end")
  }

  object fontFamily extends DS("font-family")

  object fontFeatureSettings extends DS("font-feature-settings")

  object fontKerning extends DS("font-kerning") with Auto with Normal with None {}

  object fontLanguageOverride extends DS("font-language-override") with Normal {}

  object fontOpticalSizing extends DS("font-optical-sizing") with Auto with None {}

  object fontSize extends DS("font-size") with AbsoluteSize with RelativeSize with LengthPercentage {}

  object fontSizeAdjust extends DS("font-size-adjust") with None {
    def number(d: Double): Declaration = this(s"$d")
  }

  object fontStretch extends DS("font-stretch") with FontStretchAbsolute {}

  object fontStyle extends DS("font-style") with Normal {
    def italic: Declaration = this("italic")

    object oblique {
      private def property(d: Double, suffix: String) = fontStyle.apply(s"oblique $d$suffix")
      def deg(d: Double): Declaration                 = property(d, "deg")
      def rad(d: Double): Declaration                 = property(d, "rad")
      def grad(d: Double): Declaration                = property(d, "grad")
      def turn(d: Double): Declaration                = property(d, "turn")
    }
  }

  object fontSynthesis extends DS("font-synthesis") with None {
    def weight: Declaration = this("weight")
    def style: Declaration  = this("style")
  }

  //TODO: this is incomplete - I think I could do this one
  object fontVariant extends DS("font-variant") with Normal with None {}

  //TODO: this is incomplete - I think I could do this one
  object fontVariantAlternatives extends DS("font-variant-alternatives") with Normal with None {}

  object fontVariantCaps extends DS("font-variant-caps") with Normal {
    def smallCaps: Declaration     = this("small-caps")
    def allSmallCaps: Declaration  = this("all-small-caps")
    def petiteCaps: Declaration    = this("petite-caps")
    def allPetiteCaps: Declaration = this("all-petite-caps")
    def unicase: Declaration       = this("unicase")
    def titlingCaps: Declaration   = this("titling-caps")
  }

  object fontVariantPosition extends DS("font-variant-position") with Normal {
    def sub: Declaration     = this("sub")
    def `super`: Declaration = this("super")
  }

  object fontWeight extends DS("font-weight") with FontWeightAbsolute {
    def bolder: Declaration  = this("bolder")
    def lighter: Declaration = this("lighter")
  }

  object marginLeft   extends DS("margin-left") with Auto with LengthPercentage   {}
  object marginRight  extends DS("margin-right") with Auto with LengthPercentage  {}
  object marginTop    extends DS("margin-top") with Auto with LengthPercentage    {}
  object marginBottom extends DS("margin-bottom") with Auto with LengthPercentage {}

  object textOverflow extends DS("text-overflow") {
    def clip: Declaration     = this("clip")
    def ellipsis: Declaration = this("ellipsis")
  }

  object overflow extends DS("overflow") with Auto {
    def visible: Declaration = this("visible")
    def hidden: Declaration  = this("hidden")
    def clip: Declaration    = this("clip")
    def scroll: Declaration  = this("scroll")
  }

  object top    extends DS("top") with RightLeftTopBottom    {}
  object bottom extends DS("bottom") with RightLeftTopBottom {}
  object left   extends DS("left") with RightLeftTopBottom   {}
  object right  extends DS("right") with RightLeftTopBottom  {}

  object transition extends DS(property = "transition")
  object transform  extends DS(property = "transform")

  object textTransform extends DS(property = "text-transform") with None {
    val capitalize: Declaration   = this("capitalize")
    val uppercase: Declaration    = this("uppercase")
    val lowercase: Declaration    = this("lowercase")
    val fullWidth: Declaration    = this("full-width")
    val fullSizeKana: Declaration = this("full-size-kana")
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
    def inside: Declaration  = this("inside")
    def outside: Declaration = this("outside")
  }
  //Todo: need to create URL type
  object listStyleImage extends DS(property = "list-style-image") with None

  object whiteSpace extends DS(property = "white-space") {
    def normal: Declaration  = this("normal")
    def pre: Declaration     = this("pre")
    def nowrap: Declaration  = this("nowrap")
    def preWrap: Declaration = this("pre-wrap")
    def preLine: Declaration = this("pre-line")
  }

  object userSelect extends DS(property = "user-select") with None with Auto {
    def all: Declaration     = this("all")
    def contain: Declaration = this("contain")
    def text: Declaration    = this("text")
  }

  object webkitTouchCallout extends DS(property = "-webkit-touch-callout") with None {
    def default: Declaration = this("default")
  }

  object webkitOverflowScrolling extends DS(property = "-webkit-overflow-scrolling") with Auto {
    def touch: Declaration = this("touch")
  }

  object grid            extends DS(property = "grid")
  object gridArea        extends DS(property = "grid-area")
  object gridAutoColumns extends DS(property = "grid-auto-columns")

  object gridAutoFlow extends DS(property = "grid-auto-flow") {
    def row: Declaration         = this("row")
    def column: Declaration      = this("column")
    def rowDense: Declaration    = this("row dense")
    def columnDense: Declaration = this("column dense")
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
    def visiblePainted: Declaration = this("visiblePainted")
    def visibleFill: Declaration    = this("visibleFill")
    def visibleStroke: Declaration  = this("visibleStroke")
    def visible: Declaration        = this("visible")
    def painted: Declaration        = this("painted")
  }

  object verticalAlign extends DS(property = "vertical-align") with LengthPercentage {
    def baseline: Declaration   = this("baseline")
    def sub: Declaration        = this("sub")
    def `super`: Declaration    = this("super")
    def textTop: Declaration    = this("text-top")
    def textBottom: Declaration = this("text-bottom")
    def middle: Declaration     = this("middle")
    def top: Declaration        = this("top")
    def bottom: Declaration     = this("bottom")
  }

  object resize extends DS(property = "resize") with None {
    def both: Declaration       = this("both")
    def horizontal: Declaration = this("horizontal")
    def vertical: Declaration   = this("vertical")
    def block: Declaration      = this("block")
    def inline: Declaration     = this("inline")
  }

  object letterSpacing extends DS(property = "letter-spacing") with Length {
    def normal: Declaration = this("normal")
  }

  //Todo: not complete... but should capture most use cases
  object touchAction extends DS(property = "touch-action") with Auto with None {
    def panX: Declaration         = this("pan-x")
    def panY: Declaration         = this("pan-y")
    def panLeft: Declaration      = this("pan-left")
    def panRight: Declaration     = this("pan-right")
    def panUp: Declaration        = this("pan-up")
    def panDown: Declaration      = this("pan-down")
    def pinchZoom: Declaration    = this("pinch-zoom")
    def manipulation: Declaration = this("manipulation")
    def panX_panY: Declaration    = this("pan-x pan-y")
  }

  //Todo: this should be doable as an apply normal or two LengthPercentages
  object gap extends DS(property = "gap") {
    def zero: Declaration = this("0")
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

