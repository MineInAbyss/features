package com.mineinabyss.features

import com.mineinabyss.features.properties.FeatureProvider
import com.mineinabyss.features.tasks.Task

class Features(
    val project: Project
) {
    private val features = mutableMapOf<String, DefaultFeature>()

    fun <T : DefaultFeature> register(provider: FeatureProvider<T>) {
        val feature = provider.get(project)
        features[feature.name.get()] = feature
    }

    fun get(name: String): DefaultFeature? = features[name]

    fun registerAll(vararg providers: FeatureProvider<*>) {
        providers.forEach { register(it) }
    }

    fun evaluateDependencies() {
        features.values.forEach { feature ->
            feature.dependsOn.forEach { provider ->
                val dependingTask = provider.get(project)
                feature.loadTask.dependsOn(dependingTask.loadTask)
                dependingTask.unloadTask.dependsOn(feature.unloadTask)
            }
        }
    }

    /**
     * Returns a list of tasks in execution order (dependencies first).
     * Throws IllegalStateException if a circular dependency is detected.
     */
    fun <T> resolveExecutionOrder(
        tasks: Iterable<T>,
        getDependencies: (T) -> Iterable<T>,
    ): List<T> {
        val inDegree = mutableMapOf<T, Int>()
        val dependents = mutableMapOf<T, MutableList<T>>() // Reverse graph: Dependency -> Tasks waiting on it
        val result = mutableListOf<T>()

        // 1. Build the graph and calculate in-degrees
        val allTasks = tasks.toSet() // Deduplicate
        allTasks.forEach { task ->
            inDegree.getOrPut(task) { 0 } // Ensure every task is tracked

            getDependencies(task).forEach { dependency ->
                // Direction: Dependency -> Task (Dependency must run before Task)
                dependents.getOrPut(dependency) { mutableListOf() }.add(task)
                inDegree[task] = inDegree.getOrPut(task) { 0 } + 1
            }
        }

        // 2. Initialize queue with tasks that have NO dependencies
        val queue = ArrayDeque(inDegree.filterValues { it == 0 }.keys)

        // 3. Process the queue
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            result.add(current)

            // "Unlock" tasks that depend on the current task
            dependents[current]?.forEach { dependent ->
                inDegree[dependent] = inDegree[dependent]!! - 1
                if (inDegree[dependent] == 0) {
                    queue.add(dependent)
                }
            }
        }

        // 4. Cycle Detection
        if (result.size != allTasks.size) {
            val remaining = allTasks - result.toSet()
            throw IllegalStateException("Cycle detected or missing dependencies. Unresolved: $remaining")
        }

        return result
    }

    fun callTasks(tasks: List<Task>) {
        resolveExecutionOrder(tasks) { it.dependencies }.forEach {
            it.execute()
        }
    }

    fun loadAll() {
        callTasks(features.values.map { it.loadTask })
    }

    fun unloadAll() {
        val unload = Task {
            println("Unloading features...")
        }
            .apply { dependsOn(features.values.map { it.unloadTask }) }
    }
}
