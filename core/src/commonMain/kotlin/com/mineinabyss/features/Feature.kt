package com.mineinabyss.features

import com.mineinabyss.features.impl.FeatureKeyImpl

/**
 * A dependency injection context that may be [closed][close].
 *
 * @see DI
 */
interface Feature : DI, AutoCloseable {
    fun addCloseable(closeable: AutoCloseable)

    fun addCloseable(closeable: () -> Unit) = addCloseable(AutoCloseable { closeable() })
}

/**
 * A dependency injection conte that may register new value providers and be [closed][close].
 *
 * @see Feature
 * @see MutableDI
 */
interface MutableFeature : Feature, MutableDI {
    fun dependsOn(other: FeatureKey): Feature
}

fun feature(
    name: String,
    block: MutableFeature.() -> Unit,
): FeatureKey {
    return FeatureKeyImpl(name, configure = block)
}
