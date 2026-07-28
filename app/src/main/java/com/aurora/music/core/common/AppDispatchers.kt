package com.aurora.music.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Injectable dispatchers so repositories and view models stay testable.
 * Provided by [com.aurora.music.di.CoreModule] — deliberately no `@Inject`
 * constructor, so there is exactly one binding.
 */
class AppDispatchers(
    val io: CoroutineDispatcher = Dispatchers.IO,
    val default: CoroutineDispatcher = Dispatchers.Default,
    val main: CoroutineDispatcher = Dispatchers.Main.immediate,
)
