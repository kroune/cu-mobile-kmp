package io.github.kroune.cumobile.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Injected wrapper around the standard [kotlinx.coroutines.Dispatchers].
 *
 * Exists so repositories and components can swap dispatchers in tests
 * (usually via `UnconfinedTestDispatcher`) without referencing the
 * global `Dispatchers.IO` / `Dispatchers.Default` singletons directly —
 * the detekt `InjectDispatcher` rule enforces this boundary.
 */
class AppDispatchers(
    val io: CoroutineDispatcher = Dispatchers.IO,
    val default: CoroutineDispatcher = Dispatchers.Default,
    val main: CoroutineDispatcher = Dispatchers.Main,
)
