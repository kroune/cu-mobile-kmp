package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

private val logger = KotlinLogging.logger {}

@Composable
actual fun rememberImagePicker(onImagesCaptured: (List<PickedFile>) -> Unit): ImagePicker {
    val delegate = remember { IosImageCaptureDelegate(onImagesCaptured) }
    return remember {
        object : ImagePicker {
            override fun launchCamera() {
                presentImagePickerController(
                    delegate,
                    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
                )
            }

            override fun launchGallery() {
                presentImagePickerController(
                    delegate,
                    UIImagePickerControllerSourceType
                        .UIImagePickerControllerSourceTypePhotoLibrary,
                )
            }
        }
    }
}

private fun presentImagePickerController(
    delegate: IosImageCaptureDelegate,
    sourceType: UIImagePickerControllerSourceType,
) {
    if (!UIImagePickerController.isSourceTypeAvailable(sourceType)) {
        logger.warn { "Image picker source type $sourceType not available" }
        return
    }
    val controller = UIImagePickerController().apply {
        this.sourceType = sourceType
        this.delegate = delegate
    }
    val rootVc = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootVc?.presentViewController(controller, animated = true, completion = null)
}

@Suppress("CONFLICTING_OVERLOADS")
private class IosImageCaptureDelegate(
    private val onImagesCaptured: (List<PickedFile>) -> Unit,
) : NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        try {
            val image = didFinishPickingMediaWithInfo[
                UIImagePickerControllerOriginalImage,
            ] as? UIImage ?: return

            val jpegData = UIImageJPEGRepresentation(image, compressionQuality = 0.9)
                ?: return

            val bytes = ByteArray(jpegData.length.toInt())
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), jpegData.bytes, jpegData.length)
            }
            onImagesCaptured(
                listOf(
                    PickedFile(
                        name = "photo.jpg",
                        bytes = bytes,
                        contentType = "image/jpeg",
                        size = bytes.size.toLong(),
                    ),
                ),
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to process picked image" }
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}
