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

Declarations like `single` and `factory` are _bindings_ for dependencies, under the hood you can think of your dependencies as a map where the keys are a `KType, String` pair, and values are `Lazy<T>` for that type.

These bindings are evaluated when first read, but trying to bind the same type twice will result in an error.

## single

## factory

## new

We provide some helpers for creating classes whose constructor only takes injected parameters, the `new` function takes a constructor as a lambda to get multiple dependencies automatically:

```kotlin
data class MyClass(
  val a: A,
  val b: B,
  val c: C,
  val d: D,
)

DI {
  // The following are equivalent:
  single { MyClass(get(), get(), get(), get()) }
  single { new(::MyClass) }
  singleOf(::MyClass) // (Will be added soon)
}
