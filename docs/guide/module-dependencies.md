# Inter-module dependencies

We often need modules to depend on injected values from other modules. While it's possible to manually share DI instances between modules, we also want to correctly be able to reload a module's dependants when it gets reloaded.

## `import`

Any `MutableDI` instance can `import` another `DI` context to copy its bindings. Note that since bindings can never change,
importing another context that would override an existing binding throws an error, unless the bindings are identical
(i.e. importing the same context twice will not throw an error.)

```kotlin
val parent = DI {
    single<Int> { 42 }
}
val child = DI {
    import(parent)
    single<String> { "Hello ${get<Int>}"}
}
child.get<String>() // "Hello 42"
```

## `submodule`

dependencies-kt provides helpers for modules depending on other modules which correctly handle `close` order for dependencies.
A module can load a local instance of a module via `submodule`, which will automatically close it when the parent is closed:

```kotlin
val child = module("child") {
    single("length") { get<String>().length }
    addCloseable { println("Closed child") }
}
val parent = module("parent") {
    single { "Hello" }

    val context: DI = submodule(child)
    import(context) // we can always import a submodule to get newly registered dependencies inside it
    // or import(submodule(child)) for short

    println("Length from child is ${get<Int>("length")}")
}

val scope = DIScope()
scope.load(parent) // Prints "Length from child is 5"
scope.unload(parent) // Prints "Closed child"
```

Unlike the plain `import` example above, closing the parent will correctly close the child module at the point `submodule` was called.
This guarantees that the parent can never access a closeable on the child after it has been closed.

## `singleModule`

`singleModule` acts like calling `scope.load(ParentModule)`, but ensures that when `ParentModule` gets reloaded in `scope`, this module will be reloaded too. This means multiple modules can depend on the exact same instance of a module by using `singleModule`, and that reloading this module will reload all these instances:

```kotlin
val scope = DIScope()

val parent = module("parent") {
    single<Int> { Random.nextInt() }
    println("Loaded parent!")
}
val childA = module("childA") {
    import(singleModule(parent))
    println("${get<Int>()}")
}
val childB = module("childA") {
    import(singleModule(parent))
    println("${get<Int>()}")
}

scope.load(childA) // prints "Loaded parent!", then a random int
scope.load(childB) // prints the SAME int
scope.reload(parent) // unloads childA, childB, parent. Then, loads parent, childB, childA
```
