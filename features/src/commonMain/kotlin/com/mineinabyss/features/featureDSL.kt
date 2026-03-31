package com.mineinabyss.features

import com.mineinabyss.features.properties.FeatureProvider

fun <T : DefaultFeature> feature(constructor: (Project) -> T, name: String, block: T.() -> Unit): FeatureProvider<T> {
    return FeatureProvider(name) {
        constructor(it).apply {
            this.name.set(name)
            block()
        }
    }
}