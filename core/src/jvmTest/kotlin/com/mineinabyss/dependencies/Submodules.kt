package com.mineinabyss.dependencies

import org.junit.Test

class Submodules {
    @Test
    fun `submodules example`() {
        val child = module("child") {
            single("length") { get<String>().length }
        }
        val parent = module("parent") {
            single { "Hello" }

            import(submodule(child))

            println("Length from child is ${get<Int>("length")}")
        }
        scope().load(parent)
    }

    @Test
    fun `closing a parent module closes a submodule`() {
        val child = module("child") {
            addCloseable {
                println("Child closed")
            }
        }
        val parent = module("parent") {
            submodule(child)
            addCloseable {
                println("Parent closed")
            }
        }

        DIScope().load(parent).close()
    }
}