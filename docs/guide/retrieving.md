# Retrieving

You can read bindings by calling `get`. Since bindings are evaluated on the first `get` call and can never be changed after
being registered, you typically want to delay calling `get` until as late as possible.

```kotlin
val deps = DI {
    single<String> { "Hello ${get<Int>()}"}
    single<Int> { 42 }
}
deps.get<String>() // gets "Hello 42", despite registering 42 after string

DI {
    single<String> { "Hello ${get<Int>()}"}
    get<String>() // will throw an error, since we try to evaluate string before Int is registered
    single<Int> { 42}
}
```

## Lazy delegate

For this reason, we also provide a `getLazy` helper, as well as letting you delegate your bindings directly:

```kotlin
DI {
    val integer by single<Int> { 42 }
    val string by single<String> { "Hello $integer" }
    println(string) // evaluates string, which then evaluates integer
}
```

```kotlin
val deps = DI {
    // ...
    single<MyClass> { new(::MyClass) }
}

val myClass by deps.getLazy<MyClass>() // won't evaluate right away

myClass // calls get
```

## `DI` interface

Under the hood `DI` is just an interface that takes one `DIContext` parameter, which makes it easy to delegate to:

```kotlin
class MyDependencies: DI {
    override val di: DIContext = DI {
        single<Int> { 42 }
        single<String> { "Hello ${get<Int>()}" }
    }
    
    val string by getLazy<String>()
    
    fun print() {
        println(string)
        println("Also int was ${get<Int>()}")
    }
}

MyDependencies().print() // prints "Hello 42" then "Also int was 42"
```