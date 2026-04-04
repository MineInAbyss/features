package com.mineinabyss.features

@DslMarker
annotation class FeatureDSLMarker

@FeatureDSLMarker
interface FeatureDSL
//
//fun feature(name: String, block: FeatureBuilder.() -> Unit): Feature<Unit> {
//    return FeatureBuilder(name, Unit::class).apply(block).build(extract = { })
//}
//
//@JvmName("featureWithType")
//inline fun <reified T : Any> feature(name: String, block: FeatureBuilder.() -> Unit): Feature<T> {
//    return FeatureBuilder(name, T::class).apply(block).build(extract = { instance<T>() })
//}
