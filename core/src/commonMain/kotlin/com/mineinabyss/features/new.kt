package com.mineinabyss.features

inline fun <T> DI.new(block: () -> T): T = block()
inline fun <reified A, T> DI.new(block: (A) -> T): T = block(get())
inline fun <reified A, reified B, T> DI.new(block: (A, B) -> T): T = block(get(), get())
inline fun <reified A, reified B, reified C, T> DI.new(block: (A, B, C) -> T): T = block(get(), get(), get())
inline fun <reified A, reified B, reified C, reified D, T> DI.new(block: (A, B, C, D) -> T): T = block(get(), get(), get(), get())
inline fun <reified A, reified B, reified C, reified D, reified E, T> DI.new(block: (A, B, C, D, E) -> T): T = block(get(), get(), get(), get(), get())
inline fun <reified A, reified B, reified C, reified D, reified E, reified F, T> DI.new(block: (A, B, C, D, E, F) -> T): T = block(get(), get(), get(), get(), get(), get())
inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, T> DI.new(block: (A, B, C, D, E, F, G) -> T): T = block(get(), get(), get(), get(), get(), get(), get())
inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, T> DI.new(block: (A, B, C, D, E, F, G, H) -> T): T = block(get(), get(), get(), get(), get(), get(), get(), get())
inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, T> DI.new(block: (A, B, C, D, E, F, G, H, I) -> T): T = block(get(), get(), get(), get(), get(), get(), get(), get(), get())
