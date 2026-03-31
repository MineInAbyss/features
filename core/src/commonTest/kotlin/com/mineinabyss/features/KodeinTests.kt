package com.mineinabyss.features

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.test.Test

class KodeinTests {
    @Test
    fun test() {
        val di = DI.direct {
            bindSingleton { B() }
        }
        di.instance<B>()
        di.instance<A>()
    }
}

interface A

class B: A