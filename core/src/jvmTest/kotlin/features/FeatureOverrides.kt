package features

import com.mineinabyss.features.DIScope
import com.mineinabyss.features.get
import com.mineinabyss.features.module
import com.mineinabyss.features.single
import org.junit.Test
import kotlin.test.assertEquals

class FeatureOverrides {
    @Test
    fun `features can have values overridden`() {
        // Arrange - Feature defines its own value providers
        val featureA = module("featureA") {
            val string by single { "Feature A's string" }
            val length by single<Int>("length") { string.length }

            println("Got $string with length $length")
        }

        // An override switches out the string provider
        val withOverride = featureA.override {
            single(ignoreOverride = true) { "Changed" }
        }

        val manager = DIScope()

        // Act - Loading override loads the feature with 'Changed' as though it is featureA
        val overrideInstance = manager.load(withOverride).getOrThrow()
        val featureAInstance = manager.load(featureA).getOrThrow()

        // Assert - Feature only got loaded once
        assertEquals(featureAInstance, overrideInstance)
        assertEquals(overrideInstance.get<Int>("length"), "Changed".length)
    }
}