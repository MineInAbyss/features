plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    alias(miaConventions.plugins.mia.publication)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
//            implementation(idofrontLibs.kotlinx.serialization.json)
//            implementation(libs.kodein.di)
            implementation(libs.kermit)
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
        }

        all {
            languageSettings.enableLanguageFeature("ContextParameters")
        }
    }
}