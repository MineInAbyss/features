package com.mineinabyss.features

import co.touchlab.kermit.Logger
import com.mineinabyss.features.impl.MutableDIImpl

//package com.mineinabyss.features
//
//import co.touchlab.kermit.Logger
//import org.kodein.di.*
//
////context(koinModule: Module, plugin: Plugin)
////fun singleFeatureManager(setup: FeatureManagerBuilder.() -> Unit = {}) {
////    koinModule.single { FeatureManagerBuilder(plugin).apply(setup).build(getKoin()) }
////}
//
//class FeatureManager(rootDI: DI = DI {}) {
//    private val loadedFeatures = mutableSetOf<Feature<*>>()
//    private val dependencies = mutableMapOf<Feature<*>, MutableList<Feature<*>>>()
//    private val enabledFeatures = mutableMapOf<String, FeatureInstance>()
//    private val logger = rootDI.direct.instanceOrNull<Logger>() ?: Logger
//    private val defaultModule = DI.Module("default", allowSilentOverride = true) {
//        bindSingletonOf(::FeatureContext)
//    }
//    val rootDI = DI {
//        extend(rootDI)
//        bindSingleton() { this@FeatureManager }
//    }
//
//    val loaded get() = loadedFeatures.toSet()
//
//    fun load(feature: Feature<*>) {
//        if (feature in loadedFeatures) return
//
//        feature.dependencies.features.forEach {
//            load(it)
//            val deps = dependencies.getOrPut(it) { mutableListOf() }
//            if (feature !in deps) deps.add(feature)
//        }
//
//        runCatching {
//            feature.onLoad(rootDI.direct)
//        }.onSuccess {
//            loadedFeatures += feature
//            logger.i { "Loaded feature $feature" }
//        }.onFailure {
//            logger.e(it) { "Failed to load feature $feature" }
//        }
//    }
//
//    fun loadAll(vararg features: Feature<*>) {
//        logger.i { "Loading features..." }
//        features.forEach { load(it) }
//    }
//
//    fun dependenciesMet(feature: Feature<*>): Boolean {
////        val unmetPluginDeps = feature.dependencies.plugins.filterNot(Plugins::isEnabled)
////        if (unmetPluginDeps.isNotEmpty()) {
////            logger.w { "Not loading '${feature.name}', missing dependencies: [${unmetPluginDeps.joinToString()}]" }
////            return false
////        }
//        val unmetConditions = feature.dependencies.conditions.mapNotNull {
//            runCatching { it.predicate(rootDI.direct) }.exceptionOrNull()
//        }
//        if (unmetConditions.isNotEmpty()) {
//            logger.e { "Not enabling '${feature.name}', conditions not met: [${unmetConditions.joinToString { it.message ?: "Unmet condition" }}" }
//            return false
//        }
//
//        return true
//    }
//
//    fun enable(feature: Feature<*>): Result<FeatureInstance> {
//        if (feature.name in enabledFeatures) return Result.success(enabledFeatures.getValue(feature.name))
//        load(feature)
//        if (!dependenciesMet(feature)) return Result.failure(IllegalStateException("Dependencies not met for feature"))
//
//        val di = runCatching {
//            DI {
//                feature.dependencies.features.forEach {
//                    val enabled = enabledFeatures[it.name] ?: enable(it).getOrThrow()
//                    extend(enabled.di, allowOverride = true)
//                }
//                extend(rootDI, allowOverride = true)
//                import(defaultModule, allowOverride = true)
//                feature.diBuilder(this)
//            }
//        }.onFailure {
//            logger.e(it) { "Failed to enable feature $feature, error creating its dependencies" }
//        }.getOrElse { return Result.failure(it) }
//        val instance = FeatureInstance(di)
//        logger.i { "Enabled feature '$feature'" }
//        enabledFeatures[feature.name] = instance
//        return Result.success(instance)
//    }
//
//    fun disable(feature: Feature<*>): List<Feature<*>> {
//        if (feature.name !in enabledFeatures) return emptyList()
//        val children = dependencies[feature]
//            ?.reversed()
//            ?.flatMap { disable(it) }
//            ?: emptyList()
//        runCatching { enabledFeatures.remove(feature.name)?.close() }
//            .onSuccess {
//                logger.i { "Disabled feature '$feature'" }
//            }.onFailure {
//                logger.e(it) { "Error disabling feature '$feature'" }
//            }
//        return (children + feature).distinct()
//    }
//
//    fun reload(feature: Feature<*>): Boolean {
//        val disabled = disable(feature)
//        disabled.reversed().forEach { enable(it) }
//        return true
//    }
//
//    fun enableAll() {
//        loadedFeatures.toList().forEach { enable(it) }
//    }
//
//    fun disableAll() {
//        enabledFeatures.keys.toList()
//            .forEach { name -> disable(loadedFeatures.first { it.name == name }) } //TODO cleanup
//    }
//
//    fun reloadAll() {
//        disableAll()
//        enableAll()
//    }
//
//    fun getInstance(feature: Feature<*>): FeatureInstance? = enabledFeatures[feature.name]
//
//    fun <T : Any> getOrNull(feature: Feature<T>): T? =
//        getInstance(feature)?.di?.direct?.let { feature.extract(it) }
//
//    fun <T : Any> get(feature: Feature<T>): T =
//        getOrNull(feature) ?: error("Feature $feature was not loaded")
//
//    fun getNamed(name: String): Feature<*>? = loadedFeatures.find { it.name == name }
//
//    inline fun <reified T : Any> getScoped(feature: Feature<*>): T? =
//        getInstance(feature)?.di?.direct?.let { it.instance<T>() }
//}

class FeatureManager(
    configure: MutableDI.() -> Unit = {},
) : MutableDI by MutableDIImpl() {
    private val loaded = mutableMapOf<FeatureKey, Feature>()

    init {
        single<FeatureManager>(ignoreOverride = true) { this@FeatureManager }
        configure()
    }

    val logger by single(ignoreOverride = true) { Logger }

    fun load(feature: FeatureKey): Result<Feature> {
        val key = feature.key
        if (key in loaded) return Result.success(loaded.getValue(feature))
        return runCatching { feature.load(this) }.onSuccess {
            loaded[key] = it
            logger.i { "Loaded feature $feature" }
        }.onFailure {
            if (it is IllegalArgumentException) {
                logger.e { "Failed to load feature $feature: ${it.message}" }
            } else {
                logger.e(it) { "Failed to load feature $feature" }
            }
        }
    }

    fun unload(feature: FeatureKey) {
        val key = feature.key
        val feat = loaded[key] ?: return
        runCatching { feat.close() }.onSuccess {
            logger.i { "Loaded feature $feature" }
        }.onFailure {
            logger.e(it) { "Failed to unload $feature" }
        }
        loaded.remove(key)
    }
}