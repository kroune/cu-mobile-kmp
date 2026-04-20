package io.github.kroune.cumobile.presentation.common

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Creates a lifecycle-bound [CoroutineScope] on [Dispatchers.Main.immediate] with a
 * [SupervisorJob]. Cancelled automatically when the component is destroyed.
 *
 * Use this in every `Default*Component` — it replaces the repeated boilerplate
 * `coroutineScope(Dispatchers.Main.immediate + SupervisorJob())`.
 *
 * State updates run on `Main.immediate` for minimal latency; individual
 * `launch { }` blocks should switch to [Dispatchers.IO] / [Dispatchers.Default]
 * for blocking I/O or CPU-bound work.
 */
fun ComponentContext.componentScope(): CoroutineScope =
    coroutineScope(Dispatchers.Main.immediate + SupervisorJob())
