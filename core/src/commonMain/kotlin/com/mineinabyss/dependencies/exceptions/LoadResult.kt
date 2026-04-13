package com.mineinabyss.dependencies.exceptions

import com.mineinabyss.dependencies.DI

class LoadResult(
    val results: Map<DI.Module, Result<DI>>,
) {
    val isSuccess = results.values.all { it.isSuccess }
}