# Injecting dependencies

`dependencies-kt` works around `DI` and `MutableDI` classes, the mutable variant lets you inject new dependencies, while `DI` only lets you read them. To get started, we can create a new DI context as follows:

```kotlin
val deps: DI = DI {
  // we get `MutableDI` in this context
  single<Int> { 42 }
  single<String>(key = "greeting") { "Hello ${get<Int>()}" }
}

println(deps.get<String>("greeting")) // Prints "Hello 42"
```

Declarations like `single` and `factory` are _bindings_ for dependencies. They bind a constructor to a type and string key, which gets called when first accessed via `get`. Once a binding is declared, it can never be re-declared or changed.

## Types of bindings

A `single` binding will lazily call its constructor once, and return the same value afterwards.
`factory` will evaluate its constructor every time `get` is called:

```kotlin
val deps = DI {
    factory<Int>("random") { Random.nextInt() }
}
println(deps.get<Int>("random")) // prints a random int
println(deps.get<Int>("random")) // prints a different random int
```

## Overriding dependencies

By default, registering a binding again will throw an error, unless the previous was registered with `ignoreOverride = true`.
This provides a mechanism for overriding existing dependencies:

```kotlin
val deps = DI {
    single<String>(key = "greeting", ignoreOverride = true) { "Hello plus 100: ${get<Int>() + 100}" }
    single<Int>() { 42 }
    single<String>(key = "greeting") { "Hello ${get<Int>()}" } // will ignore this binding
}

println(deps.get<String>("greeting")) // Prints "Hello plus 100: 142"
```

Notice that despite being declared above the `Int` binding, our `single<String>` is still able to read it because it is
first evaluated after the `Int` binding is called (i.e. when we call `deps.get` in the `println` line.)

## `new` helper

We provide a helper function `new` which will automatically inject all bindings for a constructor.
This is useful when adding new dependencies to a class, you may add them directly to the constructor without needing
to update any bindings, the new dependencies will automatically be injected.

```kotlin
data class MyClass(val string: string, val integer: Int)

DI {
    single<String> { "Hello" }
    single<Int> { 42 }
    single<MyClass> { new(::MyClass) }
    // Equivalent to single<MyClass> { MyClass(get(), get()) }
}
```

You may also use this outside of `single` calls to create a class without binding it:

```kotlin
DI {
    single<String> { "Hello" }
    single<Int> { 42 }
    
    val myClass = new(::MyClass)
}
```

## Missing and cyclic bindings

dependencies-kt will detect if a dependent binding did not exist, threw an error while being created, or if two bindings reference each other in a cycle when calling `get`.
When this happens, a nicely formatted error will be printed showing the chain of bindings that led to the missing call, error, or cycle.

## `DI` implements `AutoCloseable`

DI implements `AutoCloseable`, you may add instructions for how to close a DI context with `addCloseable`.
Closeables are run in reverse order to when `addCloseable` was called.

```kotlin
val deps = DI {
    val myService by single { new(::MyService) }
    
    addCloseable {
        println("Closing!")
    }
    
    // if MyService is AutoCloseable itself:
    addCloesables(myService)
}
deps.close() // Prints calls myService.close(), THEN prints "Closing!"
```