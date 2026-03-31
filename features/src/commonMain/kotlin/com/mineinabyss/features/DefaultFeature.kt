package com.mineinabyss.features

import com.mineinabyss.features.properties.BuilderProperty
import com.mineinabyss.features.properties.FeatureProvider
import com.mineinabyss.features.properties.Properties
import com.mineinabyss.features.tasks.Task
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.ScopeDSL
import org.koin.dsl.module

open class DefaultFeature(
    private val project: Project,
) {
    val properties = Properties()
    val name = properties.property<String>()

    private var loaded = false
    val loadTask = Task {
        if(loaded) {
            return@Task
        }

        main.get()
    }
    val unloadTask = Task {
        close()
    }

    val dependencies: BuilderProperty<ScopeDSL, Scope> = properties.builder { conf ->
        val koin = project.koin.get()
        val module = module {
            scope(named("test")) { conf(this) }
        }
        koin.loadModules(listOf(module))
        val scope = koin.getOrCreateScope("test", named("test"))
        onClose {
            koin.deleteScope("test")
            koin.unloadModules(listOf(module))
        }
        scope
    }.convention { }

    val onlyIf = properties.property<() -> Boolean>()

    private val main: BuilderProperty<ClosingBuilder, ClosingBuilder> = properties.builder { conf -> ClosingBuilder().apply { conf(this) } }

    private val onClose: MutableList<AutoCloseable> = mutableListOf()
    fun onClose(block: () -> Unit) {
        onClose += AutoCloseable(block)
    }

    fun main(block: ClosingBuilder.() -> Unit) {
        main.get().apply(block)
    }

    fun load() {
        main.get()
    }

    fun close() {
        onClose.reversed().forEach { it.close() }
        onClose.clear()
        properties.clear()
    }

    val dependsOn = mutableListOf<FeatureProvider<*>>()
    fun dependsOn(vararg features: FeatureProvider<*>) {
        dependsOn += features
    }

    fun onlyIf(block: () -> Boolean) {
        onlyIf.set(block)
    }

    inline fun <reified T : Any> get(): T {
        return dependencies.get().get<T>()
    }
}
