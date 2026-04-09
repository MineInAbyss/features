package com.mineinabyss.dependencies

import com.mineinabyss.dependencies.exceptions.DIBindingException
import org.junit.Test

class RecursiveCallErrors {
    data class A(val b: B)
    data class B(val a: A)

    @Test(expected = DIBindingException::class)
    fun `should throw error when types cause recursion`() {
        DI {
            single<A> { new(::A) }
            single<B> { new(::B) }
        }.get<B>()
    }
}