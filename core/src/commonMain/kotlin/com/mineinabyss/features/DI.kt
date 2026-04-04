package com.mineinabyss.features

import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A dependency injection context, values are evaluated lazily.
 *
 * A value *provider* can never change, though calling [Get] twice may return different values
 * when using a factory provider.
 */
@FeatureDSLMarker
interface DI {
    fun <T> Get(type: Pair<KType, String?>): T
    fun <T> Lazy(type: Pair<KType, String?>): Lazy<T>

    val injected: List<Pair<Pair<KType, String?>, InjectedValue<*>>>
}

/**
 * A dependency injection context that may register value providers.
 *
 * @see DI
 */
interface MutableDI : DI {
    fun <T> Put(type: Pair<KType, String?>, property: InjectedValue<T>): Lazy<T>

    fun import(context: DI)
}

/**
 * Gets a value of type [T], optionally keyed by [key].
 *
 * Immediately throws an error if not already registered.
 */
inline fun <reified T> DI.get(key: String? = null): T = Get(typeOf<T>() to key)

/**
 * Gets a value of type [T], optionally keyed by [key] as a delegate.
 * Only evaluates when first read.
 *
 * Immediately throws an error if not already registered.
 */
inline fun <reified T> DI.getLazy(key: String? = null): Lazy<T> = Lazy(typeOf<T>() to key)

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
): Lazy<T> = Put(
    typeOf<T>() to key,
    InjectedValue(ignoreOverride, lazy { block() })
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
): Lazy<T> {
    return Put(
        typeOf<T>() to key,
        InjectedValue(ignoreOverride, object : Lazy<T> {
            override val value: T get() = block()

            override fun isInitialized(): Boolean = false
        })
    )
}

context(context: MutableDI)
inline fun <reified R> Lazy<R>.and(): Lazy<R> {
    return context.single<R> { this@and.value }
}
