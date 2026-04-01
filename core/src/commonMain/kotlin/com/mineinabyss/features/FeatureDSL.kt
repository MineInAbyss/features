package com.mineinabyss.features

import org.kodein.di.DirectDI
import org.kodein.di.instance
import kotlin.jvm.JvmName

@DslMarker
annotation class FeatureDSLMarker

@FeatureDSLMarker
interface FeatureDSL

interface FeatureDI : DirectDI

fun FeatureDI.addCloseable(block: AutoCloseable) {
    instance<FeatureContext>().onClose.add(block)
}

fun FeatureDI.addCloseables(vararg closeable: AutoCloseable) {
    closeable.forEach { instance<FeatureContext>().onClose.add(it) }
}

//context(di: DirectDI)
inline fun <reified T : Any> DirectDI.get() = di.instance<T>()

fun feature(name: String, block: FeatureBuilder.() -> Unit): Feature<Unit> {
    return FeatureBuilder(name, Unit::class).apply(block).build(extract = { })
}

@JvmName("featureWithType")
inline fun <reified T : Any> feature(name: String, block: FeatureBuilder.() -> Unit): Feature<T> {
    return FeatureBuilder(name, T::class).apply(block).build(extract = { instance<T>() })
}

data class DICommandContext(val manager: FeatureManager, val feature: Feature<*>)
