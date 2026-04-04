package com.mineinabyss.features.impl

import com.mineinabyss.features.*

class MutableFeatureImpl(
    val key: FeatureKey,
    val context: MutableDI,
) : MutableFeature, MutableDI by context {
    private val closeables = mutableListOf<AutoCloseable>()
    private val dependsOn = mutableSetOf<FeatureKey>()

    override fun dependsOn(other: FeatureKey): Feature {
        val manager = context.get<FeatureManager>()
        val loaded = manager.load(other).getOrThrow()
        if (other in dependsOn) error("$key already depends on $other")
        dependsOn += other
        loaded.addCloseable { manager.unload(key) }
        return loaded
    }

    override fun addCloseable(closeable: AutoCloseable) {
        closeables += closeable
    }

    override fun close() {
        closeables.reversed().forEach { it.close() }
    }
}