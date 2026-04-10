# Modules

We often want to split dependencies into reusable modules with parts that can be swapped out for tests.


```kotlin
val MyModule = module("some name") {
  // access to `MutableDI` here
  single<Int> { Random.nextInt() } 
}

val scope = DIScope()

val di: DI = scope.load(MyModule)
val again = scope.load(MyModule)

// Module only gets loaded once per scope, so the two Random ints will be equal here!
di.get<Int>() == again.get<Int>()
```
