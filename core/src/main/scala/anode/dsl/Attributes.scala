package anode.dsl

import anode.{Attribute, Declaration}
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.Event

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.Dictionary

sealed abstract class AttributeConstructor[T](name: String) {
  def apply(t: T) = Attribute(name, t.asInstanceOf[js.Any])
}

object AttributeConstructor {
  implicit def stringToAny(s: String): js.Any = s.asInstanceOf[js.Any]
}

sealed trait AttributSet {
  type MouseHandler = js.Function1[dom.MouseEvent, Unit]

  type A[T] = AttributeConstructor[T]

  trait None extends A[String] {
    def none = this.apply("none")
  }

  trait OnOrOff extends A[String] {
    def on  = this.apply("on")
    def off = this.apply("off")
  }

  trait Sentences extends A[String] {
    def sentences = this.apply("sentences")
  }

  trait Words extends A[String] {
    def words = this.apply("words")
  }

  trait Characters extends A[String] {
    def characters = this.apply("characters")
  }

  trait Dir extends A[String] {
    def ltr  = this.apply("ltr")
    def rtl  = this.apply("rtl")
    def auto = this.apply("auto")
  }

  trait DropZone extends A[String] {
    def copy: Attribute = this.apply("copy")
    def move: Attribute = this.apply("move")
    def link: Attribute = this.apply("link")
  }

  trait InputMode extends A[String] {
    def none: Attribute    = this.apply("none")
    def text: Attribute    = this.apply("text")
    def decimal: Attribute = this.apply("decimal")
    def numeric: Attribute = this.apply("numeric")
    def tel: Attribute     = this.apply("tel")
    def search: Attribute  = this.apply("search")
    def email: Attribute   = this.apply("email")
    def url: Attribute     = this.apply("url")
  }

  trait YesOrNo extends A[String] {
    def yes: Attribute = this.apply("yes")
    def no: Attribute  = this.apply("no")
  }

  def attribute[T](name: String) = new SimpleConstructor[T](name)
  case class SimpleConstructor[T](name: String) extends AttributeConstructor[T](name)
}

trait GlobalAttributes extends AttributSet {
  def accessKey: A[String] = attribute[String](name = "accessKey")

  def autocapitalize: A[String] =
    new AttributeConstructor[String]("autocapitalize")
      with None
      with OnOrOff
      with Words
      with Sentences
      with Characters {}

  def `class`: A[String]          = attribute[String](name = "class")
  def contenteditable: A[Boolean] = attribute[Boolean](name = "contenteditable")
  def contextmenu: A[String]      = attribute[String](name = "contextmenu")

  def dir = new AttributeConstructor[String]("dir") with Dir {}

  def draggable: A[Boolean]  = attribute[Boolean](name = "draggable")
  def dropzone: A[String]    = new AttributeConstructor[String]("dropzone") with DropZone {}
  def hidden: A[Boolean]     = attribute[Boolean](name = "hidden")
  def id: A[String]          = attribute[String](name = "id")
  def inputMode: A[String]   = new AttributeConstructor[String]("inputMode") with InputMode {}
  def is: A[String]          = attribute[String](name = "is")
  def itemid: A[String]      = attribute[String](name = "itemid")
  def itemprop: A[String]    = attribute[String](name = "itemprop")
  def itemscope              = Attribute("itemscope", null)
  def itemtype: A[String]    = attribute[String](name = "itemtype")
  def lang: A[String]        = attribute[String](name = "lang")
  def slot: A[String]        = attribute[String](name = "slot")
  def spellcheck: A[Boolean] = attribute[Boolean](name = "spellcheck")
  def translate: A[String]   = new AttributeConstructor[String]("translate") with YesOrNo {}

  def style(styles: Declaration*) = {
    val d: Dictionary[String] = js.Dictionary(styles.filter(_ != null).map(x => x.property -> x.value): _*)
    Attribute("style", d)
  }

  def tabindex: A[Int] = attribute[Int](name = "tabindex")
  def title: A[String] = attribute[String](name = "title")

}

@js.native
trait InputEvent extends UIEvent {
  def data: String               = js.native
  def dataTransfer: DataTransfer = js.native
  def inputType: String          = js.native
  def isComposing: Boolean       = js.native
}

trait EventListeners extends AttributSet {
  type SideEffect        = js.Function0[Unit]
  type AnimationHandler  = js.Function1[AnimationEvent, Unit]
  type FocusHandler      = js.Function1[FocusEvent, Unit]
  type EventHandler      = js.Function1[Event, Unit]
  type ErrorHandler      = js.Function1[ErrorEvent, Unit]
  type InputHandler      = js.Function1[InputEvent, Unit]
  type KeyboardHandler   = js.Function1[KeyboardEvent, Unit]
  type PointerHandler    = js.Function1[PointerEvent, Unit]
  type UIHandler         = js.Function1[UIEvent, Unit]
  type TouchHandler      = js.Function1[TouchEvent, Unit]
  type TransitionHandler = js.Function1[TransitionEvent, Unit]
  type WheelHandler      = js.Function1[WheelEvent, Unit]

  def onAbort: A[SideEffect]                    = attribute[SideEffect](name = "onabort")
  def onAnimationCancel: A[AnimationHandler]    = attribute[AnimationHandler](name = "onanimationcancel")
  def onAnimationEnd: A[AnimationHandler]       = attribute[AnimationHandler](name = "onanimationend")
  def onAnimationIteration: A[AnimationHandler] = attribute[AnimationHandler](name = "onanimationiteration")
  def onAuxClick: A[MouseHandler]               = attribute[MouseHandler](name = "onauxclick")
  def onBlur: A[FocusHandler]                   = attribute[FocusHandler](name = "onblur")
  def onCancel: A[EventHandler]                 = attribute[EventHandler](name = "oncancel")
  def onCanPlay: A[EventHandler]                = attribute[EventHandler](name = "oncanplay")
  def onCanPlayThrough: A[EventHandler]         = attribute[EventHandler](name = "oncanplaythrough")
  def onChange: A[EventHandler]                 = attribute[EventHandler](name = "onchange")
  def onClick: A[MouseHandler]                  = attribute[MouseHandler](name = "onclick")
  def onClose: A[EventHandler]                  = attribute[EventHandler](name = "onclose")
  def onContextMenu: A[EventHandler]            = attribute[EventHandler](name = "oncontextmenu")
  def onCueChange: A[EventHandler]              = attribute[EventHandler](name = "oncuechange")
  def onDoubleClick: A[MouseHandler]            = attribute[MouseHandler](name = "ondblclick")
  def onDurationChange: A[EventHandler]         = attribute[EventHandler](name = "ondurationchange")
  def onEnded: A[EventHandler]                  = attribute[EventHandler](name = "onended")
  def onError: A[ErrorHandler]                  = attribute[ErrorHandler](name = "onerror")
  def onFocus: A[FocusHandler]                  = attribute[FocusHandler](name = "onfocus")
  def onGotPointerCapture: A[FocusHandler]      = attribute[FocusHandler](name = "ongotpointercapture")
  def onInput: A[InputHandler]                  = attribute[InputHandler](name = "oninput")
  def onInvalid: A[EventHandler]                = attribute[EventHandler](name = "oninvalid")
  def onKeyDown: A[KeyboardHandler]             = attribute[KeyboardHandler](name = "onkeydown")
  def onKeyPress: A[KeyboardHandler]            = attribute[KeyboardHandler](name = "onkeypress")
  def onKeyUp: A[KeyboardHandler]               = attribute[KeyboardHandler](name = "onkeyup")
  def onLoad: A[SideEffect]                     = attribute[SideEffect](name = "onload")
  def onLoadedData: A[EventHandler]             = attribute[EventHandler](name = "onloadeddata")
  def onLoadedMetadata: A[EventHandler]         = attribute[EventHandler](name = "onloadedmetadata")
  def onLoadEnd: A[EventHandler]                = attribute[EventHandler](name = "onloadend")
  def onLoadStart: A[EventHandler]              = attribute[EventHandler](name = "onloadstart")
  def onLostPointerCapture: A[FocusHandler]     = attribute[FocusHandler](name = "onlostpointercapture")
  def onMouseDown: A[MouseHandler]              = attribute[MouseHandler](name = "onmousedown")
  def onMouseEnter: A[MouseHandler]             = attribute[MouseHandler](name = "onmouseenter")
  def onMouseLeave: A[MouseHandler]             = attribute[MouseHandler](name = "onmouseleave")
  def onMouseMove: A[MouseHandler]              = attribute[MouseHandler](name = "onmousemove")
  def onMouseOut: A[MouseHandler]               = attribute[MouseHandler](name = "onmouseout")
  def onMouseOver: A[MouseHandler]              = attribute[MouseHandler](name = "onmouseover")
  def onMouseUp: A[MouseHandler]                = attribute[MouseHandler](name = "onmouseup")
  def onPause: A[EventHandler]                  = attribute[EventHandler](name = "onpause")
  def onPlay: A[EventHandler]                   = attribute[EventHandler](name = "onplay")
  def onPointerCancel: A[PointerHandler]        = attribute[PointerHandler](name = "onpointercancel")
  def onPointerDown: A[PointerHandler]          = attribute[PointerHandler](name = "onpointerdown")
  def onPointerEnter: A[PointerHandler]         = attribute[PointerHandler](name = "onpointerenter")
  def onPointerLeave: A[PointerHandler]         = attribute[PointerHandler](name = "onpointerleave")
  def onPointerMove: A[PointerHandler]          = attribute[PointerHandler](name = "onpointermove")
  def onPointerOut: A[PointerHandler]           = attribute[PointerHandler](name = "onpointerout")
  def onPointerOver: A[PointerHandler]          = attribute[PointerHandler](name = "onpointerover")
  def onPointerUp: A[PointerHandler]            = attribute[PointerHandler](name = "onpointerup")
  def onReset: A[EventHandler]                  = attribute[EventHandler](name = "onreset")
  def onResize: A[FocusHandler]                 = attribute[FocusHandler](name = "onresize")
  def onScroll: A[UIHandler]                    = attribute[UIHandler](name = "onscroll")
  def onSelect: A[UIHandler]                    = attribute[UIHandler](name = "onselect")
  def onSelectionChange: A[FocusHandler]        = attribute[FocusHandler](name = "onselectionchange")
  def onSelectStart: A[FocusHandler]            = attribute[FocusHandler](name = "onselectstart")
  def onSubmit: A[FocusHandler]                 = attribute[FocusHandler](name = "onsubmit")
  def onTouchCancel: A[TouchHandler]            = attribute[TouchHandler](name = "ontouchcancel")
  def onTouchStart: A[TouchHandler]             = attribute[TouchHandler](name = "ontouchstart")
  def onTouchEnd: A[TouchHandler]               = attribute[TouchHandler](name = "ontouchend")
  def onTouchMove: A[TouchHandler]              = attribute[TouchHandler](name = "ontouchmove")
  def onTransitionCancel: A[TransitionHandler]  = attribute[TransitionHandler](name = "ontransitioncancel")
  def onTransitionEnd: A[TransitionHandler]     = attribute[TransitionHandler](name = "ontransitionend")
  def onWheel: A[WheelHandler]                  = attribute[WheelHandler](name = "onwheel")
}

object Attributes extends GlobalAttributes with EventListeners {
  def href: A[String]     = attribute[String](name = "href")
  def key: A[String]      = attribute[String](name = "key")
  def `type`: A[String]   = attribute[String](name = "type")
  def `for`: A[String]    = attribute[String](name = "for")
  def value: A[String]    = attribute[String](name = "value")
  def checked: A[Boolean] = attribute[Boolean](name = "checked")

  def disabled: A[Boolean] = attribute[Boolean](name = "disabled")

  def placeholder: A[String] = attribute[String](name = "placeholder")

  def src: A[String] = attribute[String](name = "src")

  def apply(name: String, any: Any) = Attribute(name, any.asInstanceOf[js.Any])
}
