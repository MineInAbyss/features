# dependencies-kt

Simple DI framework for kotlin, designed for dependency modules that can be closed, restarted, and depend on each other.

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