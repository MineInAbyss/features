package com.mineinabyss.dependencies

import co.touchlab.kermit.Logger

class DIScope(root: MutableDI.() -> Unit = {}) : DIAware, AutoCloseable {
    private val _loaded = mutableMapOf<DI.Module.Key, DI>()
    private val loadOrder = mutableListOf<DI.Module>()
    val loaded: List<DI.Module> get() = loadOrder.toList()

    val root = DI.invoke(this) {
        single<DIScope>(ignoreOverride = true) { this@DIScope }
        root()
    }
    override val di: DI = this.root

    val logger by lazy { getOrNull<Logger>() ?: Logger }

    fun <T> load(feature: DI.ModuleWithConfig<T>, configure: T.() -> Unit): DI {
        return load(module("${feature.name}-configuration") {
            feature.get(singleModule(feature)).configure()
        })
    }

    fun load(feature: DI.Module): DI {
        val key = feature.key
        if (key in _loaded) return _loaded.getValue(key)
        val created = feature.create(root)
        _loaded[key] = created
        loadOrder += feature
        created.addCloseable {
            _loaded.remove(key)
            loadOrder.remove(feature)
            logger.i { "Unloaded feature $feature" }
        }
        logger.i { "Loaded feature $feature" }
        return created
    }

    fun loadCatching(feature: DI.Module): Result<DI> {
        return runCatching { load(feature) }.onFailure {
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

    fun reload(module: DI.Module) {
        val beforeUnload = loadOrder.toList()
        unload(module)
        val unloadedDependencies = beforeUnload.minus(loadOrder.toSet())
        loadAll(*unloadedDependencies.toTypedArray())
    }

    fun reload(vararg module: DI.Module) {
        val beforeUnload = loadOrder.toList()
        module.forEach { unload(it) }
        val unloadedDependencies = beforeUnload.minus(loadOrder.toSet())
        loadAll(*unloadedDependencies.toTypedArray())
    }

    fun reloadAll() {
        val load = loadOrder.toTypedArray()
        unloadAll()
        loadAll(*load)
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
        feat.close()
    }

    fun unloadAll() {
        loadOrder.toList().reversed().forEach { unload(it) }
    }

    override fun close() {
        unloadAll()
    }

    companion object {
        inline fun new(noinline builder: MutableDI.() -> Unit = {}): DIScope {
            return DIScope(builder)
        }
    }
}