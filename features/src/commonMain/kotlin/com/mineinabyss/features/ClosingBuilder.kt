package com.mineinabyss.features

class ClosingBuilder {
    fun addCloseables(vararg autoClose: AutoCloseable) {

    }

    fun onClose(block: () -> Unit) {
        addCloseables(AutoCloseable(block))
    }

    fun close() {

    }
}