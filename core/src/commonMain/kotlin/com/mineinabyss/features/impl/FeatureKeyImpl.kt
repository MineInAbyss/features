package com.mineinabyss.features.impl

import com.mineinabyss.features.DI
import com.mineinabyss.features.Feature
import com.mineinabyss.features.FeatureKey
import com.mineinabyss.features.MutableFeature

class FeatureKeyImpl(
    override val name: String,
    key: FeatureKey? = null,
    val configure: MutableFeature.() -> Unit,
) : FeatureKey {
    override val key = key ?: this

    override fun load(context: DI): Feature {
        return MutableDIImpl().run {
            import(context)
            val builder = MutableFeatureImpl(key, this)
            configure(builder)
            builder
        }
    }

    override fun override(beforeLoad: MutableFeature.() -> Unit): FeatureKey {
        return FeatureKeyImpl(name, key = this@FeatureKeyImpl) {
            beforeLoad()
            configure()
        }
    }

    override fun toString(): String {
        return name
    }
}