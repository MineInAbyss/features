package com.mineinabyss.features.properties

import com.mineinabyss.features.DefaultFeature
import com.mineinabyss.features.Project

class FeatureProvider<T : DefaultFeature>(
    val name: String,
    private val builder: (Project) -> T
) {
    fun get(project: Project): T {
        //TODO split build and get
        return project.features.get(name) as T? ?: builder(project)
    }
}
