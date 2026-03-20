package io.github.kroune.cumobile.presentation.common

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

@Composable
actual fun rememberImagePicker(onImagesCaptured: (List<PickedFile>) -> Unit): ImagePicker {
    val context = LocalContext.current
    val tempCameraUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = tempCameraUri.value ?: return@rememberLauncherForActivityResult
        if (!success) return@rememberLauncherForActivityResult
        handleCameraResult(context.contentResolver, uri, onImagesCaptured)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        handleGalleryResult(context.contentResolver, uris, onImagesCaptured)
    }

    return remember {
        object : ImagePicker {
            override fun launchCamera() {
                try {
                    val cameraDir = File(context.cacheDir, "camera").also { it.mkdirs() }
                    val tempFile = File(
                        cameraDir,
                        "scan_${System.currentTimeMillis()}.jpg",
                    )
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        tempFile,
                    )
                    tempCameraUri.value = uri
                    cameraLauncher.launch(uri)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to launch camera" }
                }
            }

            override fun launchGallery() {
                galleryLauncher.launch("image/*")
            }
        }
    }
}

private fun handleCameraResult(
    contentResolver: ContentResolver,
    uri: Uri,
    onImagesCaptured: (List<PickedFile>) -> Unit,
) {
    try {
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null) {
            logger.warn { "Failed to read camera image bytes" }
            return
        }
        onImagesCaptured(
            listOf(PickedFile("camera_photo.jpg", bytes, "image/jpeg", bytes.size.toLong())),
        )
    } catch (e: Exception) {
        logger.error(e) { "Failed to process camera image" }
    }
}

private fun handleGalleryResult(
    contentResolver: ContentResolver,
    uris: List<Uri>,
    onImagesCaptured: (List<PickedFile>) -> Unit,
) {
    val files = uris.mapNotNull { uri ->
        readPickedImage(contentResolver, uri)
    }
    if (files.isNotEmpty()) {
        onImagesCaptured(files)
    }
}

private fun readPickedImage(
    contentResolver: ContentResolver,
    uri: Uri,
): PickedFile? =
    try {
        val bytes = contentResolver
            .openInputStream(uri)
            ?.use { it.readBytes() }
            ?: return null
        val displayName = queryDisplayName(contentResolver, uri) ?: "image.jpg"
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        PickedFile(displayName, bytes, mimeType, bytes.size.toLong())
    } catch (e: Exception) {
        logger.error(e) { "Failed to read gallery image: $uri" }
        null
    }

private fun queryDisplayName(
    contentResolver: ContentResolver,
    uri: Uri,
): String? =
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) cursor.getString(nameIndex) else null
        } else {
            null
        }
    }
