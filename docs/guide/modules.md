# Modules

We often want to split dependencies into reusable modules with parts that can be swapped out for tests.


```kotlin
val MyModule = module("my-module") {
  // access to `MutableDI` here
  single<Int> { Random.nextInt() } 
}

val scope = DIScope()

val di: DI = scope.load(MyModule)
val again = scope.load(MyModule)

// Module only gets loaded once per scope, so the two Random ints will be equal here!
di.get<Int>() == again.get<Int>()
```

## Modules are closeable

Modules can `addCloseable` and be closed either via `scope.unload` or by calling `close` on them:

```kotlin
val MyModule = module("my-module") {
    addCloseable {
        println("Closed!")
    }
}
val scope = DIScope()
scope.load(MyModule)
scope.unload(MyModule) // prints "Closed!"

// alternatively
val di = scope.load(MyModule)
di.close() // prints "Closed!"
```
## Overriding modules

Modules can have values changed using `override`. This essentially calls the insides *before* the modle's code
runs, while still referencing the correct module key:

```kotlin
val MyModule = module("my-module") {
    val string by single { "My long string message here" }
    val length by single<Int>("length") { string.length }

    println("Got $string with length $length")
}

// An override switches out the string provider
val WithOverride = MyModule.override {
    single(ignoreOverride = true) { "Changed" }
}

val scope = DIScope()
scope.load(WithOverride) // prints "Got Changed with length 7" 
scope.load(MyModule) // Returns same DI instance as above, the keys for `withOveride` and `myModule` are equivalent

```