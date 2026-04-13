package com.mineinabyss.dependencies

import co.touchlab.kermit.Logger
import com.mineinabyss.dependencies.exceptions.LoadResult

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
            logger.i { "Unloaded module $module" }
        }
        logger.i { "Loaded module $module" }
        return created
    }

    fun loadAll(vararg modules: DI.Module) {
        modules.forEach { load(it) }
    }

    override fun reload(module: DI.Module): LoadResult {
        val beforeUnload = loadOrder.toList()
        unload(module)
        val unloadedDependencies = beforeUnload.minus(loadOrder.toSet())
        return loadAllCatching(*unloadedDependencies.toTypedArray())
    }

    fun reload(vararg module: DI.Module): LoadResult {
        val beforeUnload = loadOrder.toList()
        module.forEach { unload(it) }
        val unloadedDependencies = beforeUnload.minus(loadOrder.toSet())
        return loadAllCatching(*unloadedDependencies.toTypedArray())
    }

    fun reloadAll(): LoadResult {
        val load = loadOrder.toTypedArray()
        unloadAll()
        return loadAllCatching(*load)
    }

    fun getOrNull(module: DI.Module): DI? {
        return _loaded[module.key]?.takeIf { it != FailedModule }?.di
    }

    fun <T> getOrNull(module: DI.ModuleWithConfig<T>): T? {
        return _loaded[module.key]?.takeIf { it != FailedModule }?.di?.let { module.get(it) }
    }

    operator fun get(module: DI.Module): DI {
        return getOrNull(module) ?: error("Failed to get module ${module.name}")
    }

    operator fun <T> get(module: DI.ModuleWithConfig<T>): T {
        return getOrNull(module) ?: error("Failed to get module ${module.name}")
    }

    override fun unload(module: DI.Module) {
        val key = module.key
        val feat = _loaded[key] ?: return
        try {
            feat.close()
        } finally {
            // Ensure removed in case of FailedModule or error
            _loaded.remove(key)
            loadOrder.remove(module)
        }
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
            logger.e { "Failed to load module ${module.name}: ${it.message}" }
        } else {
            logger.e(it) { "Failed to load module ${module.name}" }
        }
    }
}

/**
 * Loads passed [modules] and their dependencies, printing error messages instead of throwing them when any fail to load.
 *
 * @return Whether all modules successfully loaded
 */
fun DI.Scope.loadAllCatching(vararg modules: DI.Module): LoadResult {
    return LoadResult(modules.associateWith { loadCatching(it) })
}
