package com.mineinabyss.features

interface FeatureKey {
    val name: String
    val key: FeatureKey

    fun load(context: DI): Feature

    fun override(beforeLoad: MutableFeature.() -> Unit): FeatureKey
}