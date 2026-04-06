plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(miaConventions.plugins.mia.autoversion)
    alias(miaConventions.plugins.mia.docs)
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://repo.mineinabyss.com/snapshots")
}