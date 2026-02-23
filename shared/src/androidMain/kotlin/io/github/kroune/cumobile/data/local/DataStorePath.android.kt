package io.github.kroune.cumobile.data.local

import android.content.Context

/**
 * Android-specific path producer for DataStore.
 * Uses the app's internal files directory.
 */
fun dataStorePath(context: Context): String = context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
