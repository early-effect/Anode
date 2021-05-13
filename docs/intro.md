# Anode Components

```scala mdoc:js:shared
import anode._
import preact.render
```

## Constructing a Simple Component

Define a stateless component function with only a property type \[String\]:

```scala mdoc:js:shared
val sayHello:Component[String] = {name =>
  E.div(s"Hello $name!",A.style(S.color("blue")))
}
```

A VNode is created when we apply a property instance to the component

```scala mdoc:js:shared
val vnode = sayHello("Russ")
```

Then we can render it by calling `render` with the VNode as well as a dom node to render within

```scala mdoc:js
render(vnode,node)
```