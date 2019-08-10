package earlyeffect.dsl

import earlyeffect.impl.Preact.{AttributeOrChildJS, ChildJS, h}
import earlyeffect.impl.{EarlyEffect, VNodeJS}
import earlyeffect.{Attribute, Args}
import earlyeffect._

import scala.scalajs.js

private[dsl] class ElementConstructor(name: String) {

  def apply(acs: Arg*): VNode = {
    val args = Args(acs)
    EarlyEffect.h(name, args.attributeDictionary, args.children)
  }
}

sealed trait ElementSet {
  def element(name: String) = new ElementConstructor(name)
}

trait Sectioning extends ElementSet {
  val address: ElementConstructor = element("address")
  val article: ElementConstructor = element("article")
  val aside: ElementConstructor   = element("aside")
  val footer: ElementConstructor  = element("footer")
  val header: ElementConstructor  = element("header")
  val h1: ElementConstructor      = element("h1")
  val h2: ElementConstructor      = element("h2")
  val h3: ElementConstructor      = element("h3")
  val h4: ElementConstructor      = element("h4")
  val h5: ElementConstructor      = element("h5")
  val h6: ElementConstructor      = element("h6")
  val hgroup: ElementConstructor  = element("hgroup")
  val main: ElementConstructor    = element("main")
  val nav: ElementConstructor     = element("nav")
  val section: ElementConstructor = element("section")
}

trait TextContent extends ElementSet {
  val blockquote: ElementConstructor = element("blockquote")
  val dd: ElementConstructor         = element("dd")
  val dir: ElementConstructor        = element("dir")
  val div: ElementConstructor        = element("div")
  val dl: ElementConstructor         = element("dl")
  val dt: ElementConstructor         = element("dt")
  val figcaption: ElementConstructor = element("figcaption")
  val figure: ElementConstructor     = element("figure")
  val hr: ElementConstructor         = element("hr")
  val li: ElementConstructor         = element("li")
  val ol: ElementConstructor         = element("ol")
  val p: ElementConstructor          = element("p")
  val pre: ElementConstructor        = element("pre")
  val ul: ElementConstructor         = element("ul")
}

trait Forms extends ElementSet {
  val button: ElementConstructor   = element("button")
  val datalist: ElementConstructor = element("datalist")
  val fieldset: ElementConstructor = element("fieldset")
  val form: ElementConstructor     = element("form")
  val input: ElementConstructor    = element("input")
  val label: ElementConstructor    = element("label")
  val legend: ElementConstructor   = element("legend")
  val meter: ElementConstructor    = element("meter")
  val optgroup: ElementConstructor = element("optgroup")
  val option: ElementConstructor   = element("option")
  val output: ElementConstructor   = element("output")
  val progress: ElementConstructor = element("progress")
  val select: ElementConstructor   = element("select")
  val textarea: ElementConstructor = element("textarea")
}

trait InlineText extends ElementSet {
  val a: ElementConstructor      = element("a")
  val abbr: ElementConstructor   = element("abbr")
  val b: ElementConstructor      = element("b")
  val bdi: ElementConstructor    = element("bdi")
  val bdo: ElementConstructor    = element("bdo")
  val br: ElementConstructor     = element("br")
  val cite: ElementConstructor   = element("cite")
  val code: ElementConstructor   = element("code")
  val data: ElementConstructor   = element("data")
  val dfn: ElementConstructor    = element("dfn")
  val em: ElementConstructor     = element("em")
  val i: ElementConstructor      = element("i")
  val kbd: ElementConstructor    = element("kbd")
  val mark: ElementConstructor   = element("mark")
  val q: ElementConstructor      = element("q")
  val rb: ElementConstructor     = element("rb")
  val rp: ElementConstructor     = element("rp")
  val rt: ElementConstructor     = element("rt")
  val rtc: ElementConstructor    = element("rtc")
  val ruby: ElementConstructor   = element("ruby")
  val s: ElementConstructor      = element("s")
  val samp: ElementConstructor   = element("samp")
  val small: ElementConstructor  = element("small")
  val span: ElementConstructor   = element("span")
  val strong: ElementConstructor = element("strong")
  val sub: ElementConstructor    = element("sub")
  val sup: ElementConstructor    = element("sup")
  val time: ElementConstructor   = element("time")
  val tt: ElementConstructor     = element("tt")
  val u: ElementConstructor      = element("u")
  val `var`: ElementConstructor  = element("var")
  val wbr: ElementConstructor    = element("wbr")
}

trait ImagesAndMultimedia extends ElementSet {
  val area: ElementConstructor  = element("area")
  val audio: ElementConstructor = element("audio")
  val img: ElementConstructor   = element("img")
  val map: ElementConstructor   = element("map")
  val track: ElementConstructor = element("track")
  val video: ElementConstructor = element("video")
}

trait EmbeddedContent extends ElementSet {
  val embed: ElementConstructor    = element("embed")
  val iframe: ElementConstructor   = element("iframe")
  val `object`: ElementConstructor = element("object")
  val param: ElementConstructor    = element("param")
  val picture: ElementConstructor  = element("picture")
  val source: ElementConstructor   = element("source")
}

trait Scripting extends ElementSet {
  val canvas: ElementConstructor   = element("canvas")
  val noscript: ElementConstructor = element("noscript")
  val script: ElementConstructor   = element("script")
}

trait DemarcatingEdits extends ElementSet {
  val del: ElementConstructor = element("del")
  val ins: ElementConstructor = element("ins")
}

trait TableContent extends ElementSet {
  val caption: ElementConstructor  = element("caption")
  val col: ElementConstructor      = element("col")
  val colgroup: ElementConstructor = element("colgroup")
  val table: ElementConstructor    = element("table")
  val tbody: ElementConstructor    = element("tbody")
  val td: ElementConstructor       = element("td")
  val tfoot: ElementConstructor    = element("tfoot")
  val th: ElementConstructor       = element("th")
  val thead: ElementConstructor    = element("thead")
  val tr: ElementConstructor       = element("tr")
}

trait InteractiveElements extends ElementSet {
  val details: ElementConstructor  = element("details")
  val dialog: ElementConstructor   = element("dialog")
  val menu: ElementConstructor     = element("menu")
  val menuitem: ElementConstructor = element("menuitem")
  val summary: ElementConstructor  = element("summary")
}

trait WebComponents extends ElementSet {
  val slot: ElementConstructor     = element("slot")
  val template: ElementConstructor = element("template")
}

object Elements
    extends Sectioning
    with TextContent
    with Forms
    with InlineText
    with ImagesAndMultimedia
    with EmbeddedContent
    with Scripting
    with DemarcatingEdits
    with TableContent
    with InteractiveElements
    with WebComponents {}
