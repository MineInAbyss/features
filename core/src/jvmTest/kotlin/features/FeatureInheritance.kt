package features

import com.mineinabyss.features.*
import org.junit.Test
import kotlin.random.Random

class FeatureInheritance {
    class Example(val string: String)

    //TODO write actual test
    @Test
    fun `should be able to create multiple features depending on each other`() {
        val featureA = feature("featureA") {
            val randomInt by factory { Random.nextInt() }
            single("configA") { "A" }
            single("configB") { "B" }
            println("Started A with $randomInt!")
            println("And got $randomInt")
        }


        val featureB = feature("featureB") {
            val string by single { "hello ${get<String>("configB")}" }
            val test by single { new(FeatureInheritance::Example) }
            import(dependsOn(featureA))
            println(test.string)

            addCloseable {
                println("Closed B, had $string!")
            }
        }
        FeatureManager().apply {
            load(featureB).getOrThrow()
            unload(featureA)
        }
    }
}