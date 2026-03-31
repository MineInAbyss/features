package com.mineinabyss.features.properties

interface BuilderProperty<B, T> : Property<T> {
    operator fun invoke(block: B.() -> Unit)

    fun convention(block: B.() -> Unit): BuilderProperty<B, T>
}

