package io.github.kroune.cumobile.data.local

import android.content.Context

/**
 * Android-specific path producer for DataStore.
 * Uses the app's internal files directory.
 */
internal fun dataStorePath(context: Context): String = context.filesDir.resolve(DataStoreFileName).absolutePath
