package com.mineinabyss.features.tasks

class Task(private val block: () -> Unit) {
    val dependencies = mutableListOf<Task>()
    fun dependsOn(task: Task) {
        dependencies += task
    }

    fun dependsOn(tasks: Collection<Task>) {
        dependencies += tasks
    }

    fun execute() {
        block()
    }
}
