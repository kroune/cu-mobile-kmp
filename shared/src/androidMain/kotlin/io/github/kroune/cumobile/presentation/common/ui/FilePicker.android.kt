package io.github.kroune.cumobile.presentation.common.ui

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Composable
actual fun rememberFilePicker(onFilePicked: (PickedFile) -> Unit): FilePicker {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val displayName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) cursor.getString(nameIndex) else null
                } else {
                    null
                }
            } ?: "file"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                logger.warn { "Failed to read file bytes from $uri" }
                return@rememberLauncherForActivityResult
            }
            onFilePicked(PickedFile(displayName, bytes, mimeType, bytes.size.toLong()))
        } catch (e: Exception) {
            logger.error(e) { "Failed to pick file from $uri" }
        }
    }
    return remember {
        object : FilePicker {
            override fun launch() {
                launcher.launch(arrayOf("*/*"))
            }
        }
    }
}
