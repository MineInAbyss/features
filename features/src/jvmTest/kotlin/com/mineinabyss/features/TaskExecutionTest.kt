package com.mineinabyss.features

import org.junit.Test

class TaskExecutionTest {
    val firstFeature = feature(::DefaultFeature, "first") {
//        dependsOn(secondFeature)
        main {
            println("ran 1")
        }

    }
    val secondFeature = feature(::DefaultFeature, "second") {
        dependsOn(firstFeature)
        main {
            println("ran 2")
        }
    }
    @Test
    fun test() {
        val project = Project()
        project.features.registerAll(
            secondFeature,
            firstFeature
        )
        project.features.evaluateDependencies()
        project.features.loadAll()
    }
}