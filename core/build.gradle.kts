plugins {
    alias(idofrontLibs.plugins.mia.kotlin.multiplatform)
    alias(idofrontLibs.plugins.mia.publication)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    wasmJs() {
        browser()
        nodejs()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            implementation(idofrontLibs.kotlinx.serialization.json)
            implementation(libs.kodein.di)
            implementation(idofrontLibs.kermit)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        all {
            languageSettings.enableLanguageFeature("ContextParameters")
        }
    }
}