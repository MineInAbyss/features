package com.mineinabyss.dependencies

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface InjectedValue<out T> : ReadOnlyProperty<Any?, T> {
    val value: T
    val ignoreOverride: Boolean

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }
}

data class InjectedValueImpl<T>(
    override val ignoreOverride: Boolean,
    val lazy: Lazy<T>,
) : InjectedValue<T> {
    override val value: T get() = lazy.value
}