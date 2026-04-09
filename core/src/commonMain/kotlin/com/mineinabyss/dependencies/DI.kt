package com.mineinabyss.dependencies

import com.mineinabyss.dependencies.DI.Companion.invoke
import com.mineinabyss.dependencies.DI.Module
import com.mineinabyss.dependencies.impl.ModuleImpl
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KType

/**
 * A dependency injection context, values are evaluated lazily.
 *
 * A value *provider* can never change, though calling [Get] twice may return different values
 * when using a factory provider.
 *
 * @see MutableDIContext
 */
interface DIContext : DI, AutoCloseable {
    val scope: DIScope

    override val di: DIContext get() = this

    fun <T> Get(type: Pair<KType, String?>): T?
    fun <T> Lazy(type: Pair<KType, String?>): InjectedValue<T>

    fun addCloseable(closeable: AutoCloseable)

    val injected: List<Pair<Pair<KType, String?>, InjectedValue<*>>>
}

/**
 * Creates a child [DI] context which has all of its parent bindings imported.
 *
 * Ensures the child context closes when the parent closes.
 */
@OptIn(ExperimentalContracts::class)
inline fun DI.subcontext(builder: MutableDI.() -> Unit = {}): DI {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }
    return invoke(di.scope) child@{
        this@subcontext.addCloseable {
            this@child.close()
        }
        import(this@subcontext.di)
        builder()
    }
}

fun module(
    name: String,
    block: MutableDI.() -> Unit = {},
): Module {
    return ModuleImpl(name, configure = block)
}

inline fun scope(noinline builder: MutableDI.() -> Unit = {}): DI.Scope {
    return DIScope(builder)
}

inline fun <reified T> Module.gets(): DI.ModuleWithConfig<T> {
    return object : DI.ModuleWithConfig<T>, Module by this {
        override fun get(parent: DI): T {
            return parent.get<T>()
        }

        override fun toString(): String {
            return name
        }
    }
}

/**
 * A dependency injection context that may register value providers.
 *
 * @see DIContext
 */
interface MutableDIContext : DIContext, MutableDI {
    override val di: MutableDIContext get() = this

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

    fun setAccessible(type: Pair<KType, String?>, accessible: Boolean)
}

