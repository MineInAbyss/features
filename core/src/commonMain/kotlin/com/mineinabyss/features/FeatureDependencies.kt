package com.mineinabyss.features

data class FeatureDependencies(
    val features: List<Feature<*>>,
    val conditions: List<LoadPredicate>,
)