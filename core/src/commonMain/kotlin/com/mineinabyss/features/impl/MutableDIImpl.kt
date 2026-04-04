package com.mineinabyss.features.impl

import com.mineinabyss.features.DI
import com.mineinabyss.features.InjectedValue
import com.mineinabyss.features.MutableDI
import kotlin.reflect.KType

class MutableDIImpl : MutableDI {
    private val _injected = mutableMapOf<Pair<KType, String?>, InjectedValue<*>>()
    override val injected get() = _injected.map { it.key to it.value }
    override fun <T> Put(type: Pair<KType, String?>, property: InjectedValue<T>): Lazy<T> {
        val existing = _injected[type]
        if (existing != null) {
            if (existing.ignoreOverride) return existing.lazy as Lazy<T>
            error("Cannot override injected type: $type")
        }
        _injected[type] = property
        return property.lazy
    }

    override fun import(context: DI) {
        context.injected.forEach { (key, value) ->
            Put(key, value)
        }
    }

    override fun <T> Get(type: Pair<KType, String?>): T {
        return _injected[type]?.lazy?.value as? T ?: error("Could not get type $type")
    }

    override fun <T> Lazy(type: Pair<KType, String?>): Lazy<T> {
        return _injected[type]?.lazy as? Lazy<T> ?: error("Could not get type $type")
    }
}