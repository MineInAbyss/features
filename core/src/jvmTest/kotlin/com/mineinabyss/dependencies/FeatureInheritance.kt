package com.mineinabyss.dependencies

import org.junit.Test
import kotlin.random.Random

class FeatureInheritance {
    class Example(val string: String)

    //TODO write actual test
    @Test
    fun `should be able to create multiple features depending on each other`() {
        val featureA = module("featureA") {
            val randomInt by factory { Random.nextInt() }
            single("configA") { "A" }
            single("configB") { "B" }
            println("Started A with $randomInt!")
            println("And got $randomInt")

            addCloseable {
                println("Closed A")
            }
        }


        val featureB = module("featureB") {
            val string by single { "hello ${get<String>("configB")}" }
            val test by single { new(FeatureInheritance::Example) }
            import(singleModule(featureA))
            println(test.string)

            addCloseable {
                println("Closed B, had $string!")
            }
        }
        val scope = DIScope()
        scope.load(featureB)
        scope.unload(featureA)
    }
}