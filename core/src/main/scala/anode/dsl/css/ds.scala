package anode.dsl.css

import anode.Declaration

object ds {

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

  trait ZeroAble extends DS {
    val zero = apply("0")
  }

  trait AbsoluteLength extends Suffixed[Double] with ZeroAble {
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
    def rgba(r: Int, g: Int, b: Int, a: Double): Declaration = apply(s"rgba($r,$g,$b,$a)")
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

  trait RightLeftTopBottom extends LengthPercentage with Auto

}
