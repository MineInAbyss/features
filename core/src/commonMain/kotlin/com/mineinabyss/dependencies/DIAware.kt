package com.mineinabyss.dependencies

import co.touchlab.kermit.Logger
import com.mineinabyss.dependencies.exceptions.DIBindingException
import com.mineinabyss.dependencies.impl.MutableDIImpl
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Delegate for a [DIContext], makes it easier for subclasses to implement
 * and get access to DI functions like [get], [getLazy], etc...
 *
 * @see DIContext
 * @see MutableDI
 */
@FeatureDSLMarker
interface DI : AutoCloseable {
    val di: DIContext

    override fun close() {
        di.close()
    }

    companion object {
        inline operator fun invoke(crossinline builder: MutableDI.() -> Unit): DIContext {
            return scope { builder() }.di
        }

        @OptIn(ExperimentalContracts::class)
        inline operator fun invoke(scope: DIScope, builder: MutableDI.() -> Unit): DIContext {
            contract {
                callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
            }
            return MutableDIImpl(scope).apply(builder)
        }
    }

    interface Module {
        val name: String
        val key: Key

        fun create(parent: DI): DI

        fun override(beforeLoad: MutableDI.() -> Unit): Module

        interface Key
    }

    interface ModuleWithConfig<T> : Module {
        fun get(parent: DI): T
    }

    interface Scope : DI {
        val logger: Logger

        fun load(module: Module): DI
        fun unload(module: Module)
        fun reload(module: Module)

        val loaded: List<Module>
    }
}


object FailedModule : DI {
    override val di: DIContext get() = error("Failed to load module")

    override fun close() {
    }
}

val DI.scope get() = di.scope
val DI.injected: List<Pair<Pair<KType, String?>, InjectedValue<*>>> get() = di.injected

inline fun DI.addCloseable(crossinline closeable: () -> Unit) {
    di.addCloseable(AutoCloseable { closeable() })
}

fun <T : AutoCloseable> DI.addCloseable(closeable: T): T {
    di.addCloseable(closeable)
    return closeable
}

fun DI.addCloseables(vararg closeables: AutoCloseable) {
    closeables.forEach { addCloseable(it) }
}

/**
 * Gets a value of type [T], optionally keyed by [key].
 *
 * Immediately throws an error if not already registered.
 */
inline fun <reified T> DI.get(key: String? = null): T = di.Get(typeOf<T>() to key) ?: throw DIBindingException.of(typeOf<T>() to key, null)

//TODO simplify
inline fun <reified T> DI.getOrNull(key: String? = null): T? = runCatching { di.Get<T>(typeOf<T>() to key) }.getOrNull()

/**
 * Gets a value of type [T], optionally keyed by [key] as a delegate.
 * Only evaluates when first read.
 *
 * Immediately throws an error if not already registered.
 */
inline fun <reified T> DI.getLazy(key: String? = null): InjectedValue<T> = di.Lazy(typeOf<T>() to key)
