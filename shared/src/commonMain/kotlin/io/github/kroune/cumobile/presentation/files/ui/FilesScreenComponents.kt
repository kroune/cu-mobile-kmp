package io.github.kroune.cumobile.presentation.files.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.ui.AppTheme

private const val MaxExtensionLength = 4

@Composable
internal fun ExtensionBadge(extension: String) {
    val badgeColor = extensionColor(extension)
    val label = if (extension.isNotEmpty() && extension.length <= MaxExtensionLength) {
        extension
    } else {
        "FILE"
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(badgeColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = badgeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Returns a color for the extension badge based on file type.
 */
@Composable
internal fun extensionColor(ext: String) =
    when (ext) {
        "PDF" -> AppTheme.colors.error
        "DOC", "DOCX" -> AppTheme.colors.taskInProgress
        "XLS", "XLSX" -> AppTheme.colors.taskEvaluated
        "PPT", "PPTX" -> AppTheme.colors.taskRework
        "ZIP", "RAR", "7Z" -> AppTheme.colors.taskReview
        "JPG", "JPEG", "PNG", "GIF", "SVG" -> AppTheme.colors.categorySoftSkills
        "MP4", "MOV", "AVI" -> AppTheme.colors.categoryBusiness
        else -> AppTheme.colors.textSecondary
    }
