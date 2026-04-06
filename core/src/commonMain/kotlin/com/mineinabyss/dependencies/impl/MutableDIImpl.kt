package com.mineinabyss.dependencies.impl

import com.mineinabyss.dependencies.*
import com.mineinabyss.dependencies.exceptions.DIBindingException
import kotlin.reflect.KType

class MutableDIImpl(
    override val scope: DIScope,
) : MutableDI {
    private val closeables = mutableListOf<AutoCloseable>()
    private val dependsOn = mutableSetOf<DI.Module>()
    private val _injected = mutableMapOf<Pair<KType, String?>, InjectedValue<*>>()
    override val injected get() = _injected.map { it.key to it.value }
    private var closed = false

    override fun <T> Put(type: Pair<KType, String?>, property: InjectedValue<T>): InjectedValue<T> {
        val existing = _injected[type]
        if (existing != null) {
            if (existing.ignoreOverride || existing == property) return existing as InjectedValue<T>
            error("Cannot override injected type: $type")
        }
        _injected[type] = property
        return property
    }

    override fun <T> Get(type: Pair<KType, String?>): T {
        return _injected[type]?.value as? T ?: throw DIBindingException.of(type, null)
    }

    override fun <T> Lazy(type: Pair<KType, String?>): InjectedValue<T> {
        return _injected[type] as? InjectedValue<T> ?: throw DIBindingException.of(type, null)
    }


    override fun singleModule(di: DI.Module): DI {
        val loaded = scope.load(di)
        if (di in dependsOn) return loaded
        dependsOn += di
        loaded.addCloseable { this@MutableDIImpl.close() }
        return loaded
    }

    override fun submodule(di: DI.Module): DI {
        if (di in dependsOn) error("Cannot create a submodule twice")
        dependsOn += di
        val newInstance = di.create(this)
        addCloseable { newInstance.close() }
        return newInstance
    }

    override fun import(context: DI) {
        context.injected.forEach { (key, value) ->
            Put(key, value)
        }
    }

    override fun addCloseable(closeable: AutoCloseable) {
        closeables += closeable
    }

    override fun close() {
        if (closed) return
        closed = true
        closeables.reversed().forEach { it.close() }
    }
}