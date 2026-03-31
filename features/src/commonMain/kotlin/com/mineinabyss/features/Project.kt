package com.mineinabyss.features

import com.mineinabyss.features.properties.FeatureProvider
import com.mineinabyss.features.properties.Properties
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

class Project {
    val properties = Properties()
    val koin = properties.builder<KoinApplication, Koin> { configure ->
        koinApplication { configure() }.koin
    }.convention { }

    val features = Features(this)

    inline fun <reified T: Any> get(): T = koin.get().get<T>()

    inline fun <T : DefaultFeature> register(featureProvider: FeatureProvider<T>, configure: T.() -> Unit = {}) {

    }

    fun main(block: () -> Unit) {

    }
}