package com.mineinabyss.features

data class InjectedValue<T>(
    val ignoreOverride: Boolean,
    val lazy: Lazy<T>,
)