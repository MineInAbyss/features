package com.mineinabyss.features

import com.mineinabyss.features.DI.Module
import com.mineinabyss.features.impl.ModuleImpl
import com.mineinabyss.features.impl.MutableDIImpl
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface DIAware : AutoCloseable {
    val di: DI

    override fun close() {
        di.close()
    }
}

/**
 * A dependency injection context, values are evaluated lazily.
 *
 * A value *provider* can never change, though calling [Get] twice may return different values
 * when using a factory provider.
 */
@FeatureDSLMarker
interface DI : DIAware, AutoCloseable {
    val scope: DIScope

    override val di: DI get() = this

    fun <T> Get(type: Pair<KType, String?>): T
    fun <T> Lazy(type: Pair<KType, String?>): InjectedValue<T>

    fun addCloseable(closeable: AutoCloseable)

    val injected: List<Pair<Pair<KType, String?>, InjectedValue<*>>>

    interface Module {
        val name: String
        val key: Key

        fun create(parent: DI): DI

        fun override(beforeLoad: MutableDI.() -> Unit): Module

        interface Key
    }

    fun childDI(): DI {
        val di = invoke(di.scope) { import(this@DI.di) }
        addCloseable(di)
        return di
    }

    interface ModuleWithConfig<T> : Module {
        fun get(parent: DI): T
    }

    companion object {
        operator fun invoke(scope: DIScope = DIScope(), builder: MutableDI.() -> Unit): DI {
            return MutableDIImpl(scope).apply(builder)
        }
    }
}

fun module(
    name: String,
    block: MutableDI.() -> Unit,
): Module {
    return ModuleImpl(name, configure = block)
}

inline fun DIAware.addCloseable(crossinline closeable: () -> Unit) {
    di.addCloseable(AutoCloseable { closeable() })
}

fun <T : AutoCloseable> DIAware.addCloseable(closeable: T): T {
    di.addCloseable(closeable)
    return closeable
}

fun DIAware.addCloseables(vararg closeables: AutoCloseable) {
    closeables.forEach { addCloseable(it) }
}

inline fun <reified T> Module.gets(): DI.ModuleWithConfig<T> {
    return object : DI.ModuleWithConfig<T>, Module by this {
        override fun get(parent: DI): T {
            return parent.get<T>()
        }
    }
}

/**
 * A dependency injection context that may register value providers.
 *
 * @see DI
 */
interface MutableDI : DI {
    fun <T> Put(type: Pair<KType, String?>, property: InjectedValue<T>): InjectedValue<T>

    /**
     * Loads a [DI.Module] as a singleton in this DI [scope]. If a module is already loaded, gets the instance.
     *
     * Closes this DI context when the included module is unloaded in [scope].
     */
    fun singleModule(di: Module): DI

    /**
     * Loads a [DI.Module] inside this [DI] context, copies dependencies at the point of the call into the submodule,
     * but does not [import] the submodule's new dependencies into this context.
     *
     * Closes the submodule when closing this DI context.
     *
     * @see import
     */
    fun submodule(di: Module): DI

    /**
     * Imports all keys from a DI [context] into this context.
     * Keys can never be overridden, so this may throw an error if the included context contains clashing providers.
     *
     * Identical providers do not throw an error, thus a submodule can be included via:
     *
     * ```kotlin
     * import(submodule(other))
     * ```
     */
    fun import(context: DI)
}

/**
 * Gets a value of type [T], optionally keyed by [key].
 *
 * Immediately throws an error if not already registered.
 */
inline fun <reified T> DIAware.get(key: String? = null): T = di.Get(typeOf<T>() to key)

//TODO simplify
inline fun <reified T> DIAware.getOrNull(key: String? = null): T? = runCatching { di.Get<T>(typeOf<T>() to key) }.getOrNull()

/**
 * Gets a value of type [T], optionally keyed by [key] as a delegate.
 * Only evaluates when first read.
 *
 * Immediately throws an error if not already registered.
 */
inline fun <reified T> DIAware.getLazy(key: String? = null): InjectedValue<T> = di.Lazy(typeOf<T>() to key)

/**
 * Registers a value of type [T], optionally keyed by [key].
 *
 * Throws an error if already registered, unless the existing key was marked with [ignoreOverride],
 * in this case redirects to the existing value.
 */
inline fun <reified T> MutableDI.single(
    key: String? = null,
    ignoreOverride: Boolean = false,
    crossinline block: DI.() -> T,
): InjectedValue<T> = Put(
    typeOf<T>() to key,
    InjectedValueImpl(ignoreOverride, lazy { block() })
)

/**
 * Registers a factory function for type [T], optionally keyed by [key].
 * Each [get] request will create a new value.
 *
 * Throws an error if already registered, unless the existing key was marked with [ignoreOverride],
 * in this case redirects to the existing value.
 */
inline fun <reified T> MutableDI.factory(
    key: String? = null,
    ignoreOverride: Boolean = false,
    noinline block: DI.() -> T,
): InjectedValue<T> {
    return Put(
        typeOf<T>() to key,
        InjectedValueImpl(ignoreOverride, object : Lazy<T> {
            override val value: T get() = block()

            override fun isInitialized(): Boolean = false
        })
    )
}

context(context: MutableDI)
inline fun <reified R> InjectedValue<R>.and(): InjectedValue<R> {
    return context.single<R>(ignoreOverride = ignoreOverride) { this@and.value }
}
