package com.mineinabyss.dependencies

import com.mineinabyss.dependencies.exceptions.DIBindingException
import kotlin.reflect.typeOf

/**
 * Delegate for a [MutableDIContext], makes it easier for subclasses to implement
 * and get access to DI functions like [get], [single], [factory], etc...
 *
 * @see MutableDIContext
 * @see DI
 */
interface MutableDI : DI {
    override val di: MutableDIContext
}

fun MutableDI.import(context: DI) = di.import(context)
fun MutableDI.singleModule(module: DI.Module) = di.singleModule(module)
fun MutableDI.submodule(module: DI.Module) = di.submodule(module)

context(context: MutableDI)
inline fun <reified R> InjectedValue<R>.and(): InjectedValue<R> {
    return context.single<R>(ignoreOverride = ignoreOverride) { this@and.value }
}

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
): InjectedValue<T> = di.Put(
    typeOf<T>() to key,
    InjectedValueImpl(ignoreOverride, lazy {
        try {
            block(di)
        } catch (e: Throwable) {
            throw DIBindingException.of(typeOf<T>() to key, e)
        }
    })
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
    return di.Put(
        typeOf<T>() to key,
        InjectedValueImpl(ignoreOverride, object : Lazy<T> {
            override val value: T
                get() = try {
                    block(di)
                } catch (e: Throwable) {
                    throw DIBindingException.of(typeOf<T>() to key, e)
                }

            override fun isInitialized(): Boolean = false
        })
    )
}