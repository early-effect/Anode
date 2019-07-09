package earlyeffect.dsl

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object AutoPrefixed {

  object Options extends js.Object {
    def browsers = js.Array("last 2 versions", "> 5%")
  }

  val Processor = new PostCSS(js.Array(new AutoPrefixer(Options)))

  @js.native @JSImport("postcss", JSImport.Namespace)
  class PostCSS(arg: js.Array[js.Any]) extends js.Object {
    def process(s: String, options: js.Any): Result = js.native
  }

  @js.native @JSImport("autoprefixer", JSImport.Namespace)
  class AutoPrefixer(a: js.Any) extends js.Object

  trait Result extends js.Object {
    def css: String
    def result: Result
    def toString: String
  }

  def apply(css: String): String =
    try {
      Processor.process(css, Options).css
    } catch {
      case e: js.JavaScriptException =>
        js.Dynamic.global.console.error("Bad CSS\n", css, e.asInstanceOf[js.Any])
        css
    }

}
