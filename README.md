# dependencies-kt

Simple DI framework for Kotlin, designed for dependency modules that can be closed, restarted, and depend on each other.

## Usage

Add the project in gradle, supported KMP platforms are listed in [the buildscript](core/build.gradle.kts)

```kotlin
repositories {
    maven("https://repo.mineinabyss.com/releases")
}

dependencies {
    implementation("com.mineinabyss.dependencies:core:x.y.z")
}
```

## Quick example

```kotlin
class MyClass(val text: String)

val root = DI {
    single<String> { "Hello world" }
    val myClass by single { new(::MyClass) }
    
    println("Starting with ${myClass.text}")
    
    addCloseable {
      println("Closing!")
    }
} // Prints "Starting with Hello World"

val string = root.get<String>() // Gets "Hello World"

root.close() // Prints "Closing!"
```
