package com.mineinabyss.features.impl

import com.mineinabyss.features.DI
import com.mineinabyss.features.DI.Module
import com.mineinabyss.features.MutableDI

class ModuleImpl(
    override val name: String,
    key: Module.Key? = null,
    val configure: MutableDI.() -> Unit,
) : Module, Module.Key {
    override val key = key ?: this

    override fun create(parent: DI): DI = DI(parent.scope) {
        import(parent)
        configure(this)
    }

    override fun override(beforeLoad: MutableDI.() -> Unit): Module {
        return ModuleImpl(name, key = this@ModuleImpl) {
            beforeLoad()
            configure()
        }
    }

    override fun toString(): String {
        return name
    }
}