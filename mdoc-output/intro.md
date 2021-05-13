# Anode Components

```scala
import anode._
import preact.render
```

## Constructing a Simple Component

Define a stateless component function with only a property type \[String\]:

```scala
val sayHello:Component[String] = {name =>
  E.div(s"Hello $name!",A.style(S.color("blue")))
}
```

A VNode is created when we apply a property instance to the component

```scala
val vnode = sayHello("Russ")
```

Then we can render it by calling `render` with the VNode as well as a dom node to render within

```scala
render(vnode,node)
```
<div id="mdoc-html-run3" data-mdoc-js></div>
<script type="text/javascript" src="jsdocs-opt-library.js" defer></script>
<script type="text/javascript" src="jsdocs-opt-loader.js" defer></script>
<script type="text/javascript" src="intro.md.js" defer></script>
<script type="text/javascript" src="mdoc.js" defer></script>

