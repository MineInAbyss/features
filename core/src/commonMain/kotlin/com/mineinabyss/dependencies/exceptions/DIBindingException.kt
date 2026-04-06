package com.mineinabyss.dependencies.exceptions

import kotlin.reflect.KClass
import kotlin.reflect.KType

class DIBindingException(
    val types: List<Pair<KType, String?>>,
    cause: Throwable?,
) : IllegalStateException(cause) {
    override val message: String get() = prettyPrintTypes(types)

    private fun prettyPrintTypes(types: List<Pair<KType, String?>>): String {
        if (types.size == 1) {
            return if (cause == null) "Could not find type ${prettyPrint(types.first())}"
            else "Error while creating type ${prettyPrint(types.first())}"
        }
        return buildString {
            if (cause != null) appendLine("Could not create type ${prettyPrint(types.last())}, it depends on ${prettyPrint(types.first())} which threw an error")
            else appendLine("Could not create type ${prettyPrint(types.last())}, it depends on ${prettyPrint(types.first())} which was not found")
            types.forEachIndexed { index, pair ->
                if (index != 0) {
                    repeat(index * 2 - 1) { append(' ') }
                    append("⬑")
                }
                append(prettyPrint(pair, simpleName = false))
                if (index != types.lastIndex) appendLine()
            }
        }
//        return prettyPrintTypes(buildString {
//            if (types.size > 1) append("↳")
//            appendLine(prettyPrint(types.first()))
//            if (string != null) append(string.prependIndent(" "))
//        }, types.drop(1))
    }

    private fun prettyPrint(type: Pair<KType, String?>, simpleName: Boolean = true) = buildString {
        if (simpleName) append((type.first.classifier as KClass<*>).simpleName)
        else append((type.first.classifier as KClass<*>).qualifiedName)
        if (type.second != null) append("(key=${type.second})")
    }

    companion object {
        inline fun of(type: Pair<KType, String?>, cause: Throwable?): DIBindingException {
            return if (cause is DIBindingException) DIBindingException(
                cause.types + type, cause.cause
            )
            else DIBindingException(listOf(type), cause)
        }
    }
}