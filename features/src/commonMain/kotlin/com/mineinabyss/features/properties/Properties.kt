package com.mineinabyss.features.properties

class Properties {
    private val evaluatedProperties = mutableMapOf<String, Any>()
    fun <B, T> builder(builder: (B.() -> Unit) -> T): BuilderProperty<B, T> {
        return DefaultBuilderProperty(builder)
    }

    fun <T> property(): Property<T> {
        return DefaultProperty()
    }

    fun clear() {

    }

}

class DefaultBuilderProperty<B, T>(
    val build: ((B) -> Unit) -> T,
) : DefaultProperty<T>(), BuilderProperty<B, T> {
    var configureBlock: (B) -> Unit = {}
    override fun get(): T {
        super.getOrNull()?.let { return it }
        val built = build(configureBlock)
        set(built)
        return built
    }

    override fun invoke(block: B.() -> Unit) {
        configureBlock = block
    }

    override fun convention(block: B.() -> Unit): BuilderProperty<B, T> {
        configureBlock = block
        return this
    }
}

open class DefaultProperty<T> : Property<T> {
    private var evaluated: T? = null
    private var provider: Provider<T>? = null
    override fun set(value: T) {
        this.evaluated = value
    }

    override fun set(provider: Provider<T>) {
        TODO()
    }

    override fun convention(provider: Provider<T>): Property<T> {
        TODO("Not yet implemented")
    }

    override fun convention(value: T): Property<T> {
        TODO("Not yet implemented")
    }

    override fun unset(): Property<T> {
        TODO("Not yet implemented")
    }

    override fun get(): T {
        return evaluated!!
    }

    override fun getOrNull(): T? {
        return evaluated
    }

    override fun <R> map(block: (T) -> R): Provider<R> {
        TODO("Not yet implemented")
    }
}