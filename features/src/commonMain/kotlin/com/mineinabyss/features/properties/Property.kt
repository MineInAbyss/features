package com.mineinabyss.features.properties

interface Property<T> : Provider<T> {
    fun set(value: T)

    fun set(provider: Provider<T>)
    fun convention(provider: Provider<T>): Property<T>
    fun convention(value: T): Property<T>

    fun unset(): Property<T>
}
