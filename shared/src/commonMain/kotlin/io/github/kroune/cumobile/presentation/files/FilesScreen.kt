@file:Suppress("TooManyFunctions", "MagicNumber")

package io.github.kroune.cumobile.presentation.files

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.local.DownloadedFileInfo
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
import io.github.kroune.cumobile.presentation.common.formatEpochDate
import io.github.kroune.cumobile.presentation.common.formatSizeBytes

/**
 * Files tab screen displaying locally downloaded files.
 *
 * Shows a list of files with extension badges, filename, size,
 * and modification date. Supports tap-to-open, long-press-to-select,
 * and batch/single delete actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    component: FilesComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        ConfirmDeleteDialog(
            title = "Удалить все файлы?",
            text = "Это действие удалит все ${state.files.size} загруженных файлов.",
            onConfirm = {
                showDeleteAllDialog = false
                component.onIntent(FilesComponent.Intent.DeleteAll)
            },
            onDismiss = { showDeleteAllDialog = false },
        )
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { component.onIntent(FilesComponent.Intent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            FilesHeader(
                state = state,
                onDeleteAll = { showDeleteAllDialog = true },
                onDeleteSelected = {
                    component.onIntent(FilesComponent.Intent.DeleteSelected)
                },
                onClearSelection = {
                    component.onIntent(FilesComponent.Intent.ClearSelection)
                },
                onOpenRenameSettings = {
                    component.onIntent(FilesComponent.Intent.OpenRenameSettings)
                },
            )

            when {
                state.isLoading && state.files.isEmpty() -> LoadingContent()
                state.error != null && state.files.isEmpty() -> ErrorContent(
                    error = state.error.orEmpty(),
                    onRetry = { component.onIntent(FilesComponent.Intent.Refresh) },
                )
                state.files.isEmpty() -> EmptyState(
                    onOpenRenameSettings = {
                        component.onIntent(FilesComponent.Intent.OpenRenameSettings)
                    },
                )
                else -> FileList(
                    files = state.files,
                    selectedFiles = state.selectedFiles,
                    onOpen = { path ->
                        component.onIntent(FilesComponent.Intent.OpenFile(path))
                    },
                    onToggleSelect = { name ->
                        component.onIntent(FilesComponent.Intent.ToggleSelect(name))
                    },
                    onDelete = { name ->
                        component.onIntent(FilesComponent.Intent.DeleteFile(name))
                    },
                )
            }
        }
    }
}

@Composable
private fun FilesHeader(
    state: FilesComponent.State,
    onDeleteAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onClearSelection: () -> Unit,
    onOpenRenameSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Файлы",
                color = AppColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            if (state.files.isNotEmpty()) {
                Text(
                    text = "${state.files.size} файл(ов) • ${
                        formatSizeBytes(state.totalSizeBytes)
                    }",
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }

        if (state.isSelecting) {
            Row {
                TextButton(onClick = onDeleteSelected) {
                    Text(
                        text = "Удалить (${state.selectedFiles.size})",
                        color = AppColors.Error,
                        fontSize = 14.sp,
                    )
                }
                TextButton(onClick = onClearSelection) {
                    Text(
                        text = "Отмена",
                        color = AppColors.TextSecondary,
                        fontSize = 14.sp,
                    )
                }
            }
        } else if (state.files.isNotEmpty()) {
            Row {
                TextButton(onClick = onOpenRenameSettings) {
                    Text(
                        text = "⚙",
                        color = AppColors.Accent,
                        fontSize = 18.sp,
                    )
                }
                TextButton(onClick = onDeleteAll) {
                    Text(
                        text = "Очистить",
                        color = AppColors.Error,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun FileList(
    files: List<DownloadedFileInfo>,
    selectedFiles: Set<String>,
    onOpen: (path: String) -> Unit,
    onToggleSelect: (name: String) -> Unit,
    onDelete: (name: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(files, key = { it.name }) { file ->
            FileRow(
                file = file,
                isSelected = file.name in selectedFiles,
                isSelecting = selectedFiles.isNotEmpty(),
                onTap = { onOpen(file.path) },
                onLongPress = { onToggleSelect(file.name) },
                onDelete = { onDelete(file.name) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(
    file: DownloadedFileInfo,
    isSelected: Boolean,
    isSelecting: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit,
) {
    val backgroundColor = when {
        isSelected -> AppColors.Accent.copy(alpha = 0.15f)
        else -> AppColors.Surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    if (isSelecting) onLongPress() else onTap()
                },
                onLongClick = onLongPress,
            ).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExtensionBadge(extension = file.extension)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${formatSizeBytes(file.sizeBytes)} • ${
                    formatEpochDate(file.lastModifiedMillis)
                }",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
            )
        }
        if (!isSelecting) {
            TextButton(onClick = onDelete) {
                Text(
                    text = "×",
                    color = AppColors.TextSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ExtensionBadge(extension: String) {
    val badgeColor = extensionColor(extension)
    val label = if (extension.isNotEmpty() && extension.length <= 4) {
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

@Composable
private fun EmptyState(
    onOpenRenameSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.TextSecondary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "FILE",
                color = AppColors.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Нет загруженных файлов",
            color = AppColors.TextPrimary,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Скачанные материалы появятся здесь",
            color = AppColors.TextSecondary.copy(alpha = 0.6f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onOpenRenameSettings,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Настроить шаблоны", color = AppColors.Background)
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(text = title, color = AppColors.TextPrimary)
        },
        text = {
            Text(text = text, color = AppColors.TextSecondary)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Удалить", color = AppColors.Error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Отмена", color = AppColors.TextSecondary)
            }
        },
    )
}

/**
 * Returns a color for the extension badge based on file type.
 */
private fun extensionColor(ext: String) =
    when (ext) {
        "PDF" -> AppColors.Error
        "DOC", "DOCX" -> AppColors.TaskInProgress
        "XLS", "XLSX" -> AppColors.TaskEvaluated
        "PPT", "PPTX" -> AppColors.TaskRework
        "ZIP", "RAR", "7Z" -> AppColors.TaskReview
        "JPG", "JPEG", "PNG", "GIF", "SVG" -> AppColors.CategorySoftSkills
        "MP4", "MOV", "AVI" -> AppColors.CategoryBusiness
        else -> AppColors.TextSecondary
    }
