package com.mineinabyss.dependencies

import co.touchlab.kermit.Logger

class DIScope(root: MutableDI.() -> Unit = {}) : DI.Scope, DI, AutoCloseable {
    private val _loaded = mutableMapOf<DI.Module.Key, DI>()
    private val loadOrder = mutableListOf<DI.Module>()
    override val loaded: List<DI.Module> get() = loadOrder.toList()

    val root = DI.invoke(this) {
        single<DIScope>(ignoreOverride = true) { this@DIScope }
        root()
        addCloseable { this@DIScope.unloadAll() }
    }

    override val di: DIContext = this.root.di

    override val logger by lazy { getOrNull<Logger>() ?: Logger }

    fun <T> load(module: DI.ModuleWithConfig<T>, configure: T.() -> Unit): DI {
        return load(module("${module.name}-configuration") {
            module.get(singleModule(module)).configure()
        })
    }

    override fun load(module: DI.Module): DI {
        val key = module.key
        if (key in _loaded) return _loaded.getValue(key)
        val created = runCatching { module.create(root) }.onFailure {
            _loaded[key] = FailedModule
            loadOrder += module
        }.getOrThrow()
        _loaded[key] = created
        loadOrder += module
        created.addCloseable {
            _loaded.remove(key)
            loadOrder.remove(module)
            logger.i { "Unloaded feature $module" }
        }
        logger.i { "Loaded feature $module" }
        return created
    }

    fun loadAll(vararg modules: DI.Module) {
        modules.forEach { load(it) }
    }

    override fun reload(module: DI.Module) {
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

    override fun unload(module: DI.Module) {
        val key = module.key
        val feat = _loaded[key] ?: return
        feat.close()
        // Ensure removed in case of FailedModule
        _loaded.remove(key)
        loadOrder.remove(module)
    }

    fun unloadAll() {
        loadOrder.toList().reversed().forEach { unload(it) }
    }

    override fun close() {
        unloadAll()
    }
}

fun DI.Scope.loadCatching(module: DI.Module): Result<DI> {
    return runCatching { load(module) }.onFailure {
        if (it is IllegalArgumentException) {
            logger.e { "Failed to load feature $module: ${it.message}" }
        } else {
            logger.e(it) { "Failed to load feature $module" }
        }
    }
}

fun DI.Scope.loadAllCatching(vararg modules: DI.Module) {
    modules.forEach { loadCatching(it) }
}
