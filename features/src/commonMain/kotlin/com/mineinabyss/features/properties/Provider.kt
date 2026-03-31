package com.mineinabyss.features.properties

interface Provider<T> {
    fun get(): T

    fun getOrNull(): T?

    fun <R> map(block: (T) -> R): Provider<R>
}