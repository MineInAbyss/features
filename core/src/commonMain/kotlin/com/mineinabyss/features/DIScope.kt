package com.mineinabyss.features

import co.touchlab.kermit.Logger

class DIScope(root: MutableDI.() -> Unit = {}) : DIAware, AutoCloseable {
    private val _loaded = mutableMapOf<DI.Module.Key, DI>()
    val loaded: List<DI.Module> = _loaded.keys.filterIsInstance<DI.Module>()

    val root = DI.invoke(this) {
        single<DIScope>(ignoreOverride = true) { this@DIScope }
        single<Logger>(ignoreOverride = true) { Logger }
        root()
    }
    override val di: DI = this.root

    val logger by getLazy<Logger>()

    fun <T> load(feature: DI.ModuleWithConfig<T>, configure: T.() -> Unit): DI {
        return load(module("$feature-configuration") {
            feature.get(singleModule(feature)).configure()
        })
    }

    fun load(feature: DI.Module): DI {
        val key = feature.key
        if (key in _loaded) return _loaded.getValue(key)
        return loadCatching(feature).getOrThrow()
    }

    fun loadCatching(feature: DI.Module): Result<DI> {
        val key = feature.key
        if (key in _loaded) return Result.success(_loaded.getValue(key))
        return runCatching { feature.create(root) }.onSuccess {
            _loaded[key] = it
            it.addCloseable { _loaded.remove(key) }
            logger.i { "Loaded feature $feature" }
        }.onFailure {
            if (it is IllegalArgumentException) {
                logger.e { "Failed to load feature $feature: ${it.message}" }
            } else {
                logger.e(it) { "Failed to load feature $feature" }
            }
        }
    }

    fun loadAll(vararg modules: DI.Module) {
        modules.forEach { load(it) }
    }

    fun loadAllCatching(vararg modules: DI.Module) {
        modules.forEach { loadCatching(it) }
    }

    operator fun get(module: DI.Module): DI? {
        return _loaded[module.key]?.di
    }

    operator fun <T> get(module: DI.ModuleWithConfig<T>): T? {
        return _loaded[module.key]?.di?.let { module.get(it) }
    }

    fun unload(feature: DI.Module) {
        val key = feature.key
        val feat = _loaded[key] ?: return
        runCatching { feat.close() }.onSuccess {
            logger.i { "Loaded feature $feature" }
        }.onFailure {
            logger.e(it) { "Failed to unload $feature" }
        }
        _loaded.remove(key)
    }

    override fun close() {
        _loaded.toList().reversed().forEach { unload(it.first as DI.Module) } //TODO no cast
    }

    companion object {
        fun new(builder: MutableDI.() -> Unit = {}): DIScope {
            return DIScope(builder)
        }
    }
}