package io.github.kroune.cumobile.presentation.scanner.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.presentation.common.decodeImageBitmap
import io.github.kroune.cumobile.presentation.common.formatSizeBytes
import io.github.kroune.cumobile.presentation.common.ui.ActionErrorBar
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ui.ImagePicker
import io.github.kroune.cumobile.presentation.common.ui.rememberImagePicker
import io.github.kroune.cumobile.presentation.scanner.ScannerComponent

@Composable
fun ScannerScreen(
    component: ScannerComponent,
    onBack: () -> Unit,
) {
    val state by component.state.subscribeAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        component.effects.collect { effect ->
            when (effect) {
                is ScannerComponent.Effect.ShowError -> actionError = effect.message
                ScannerComponent.Effect.SaveSuccess -> Unit
            }
        }
    }

    val imagePicker = rememberImagePicker { images ->
        component.onIntent(ScannerComponent.Intent.Page.Add(images))
    }

    ScannerScreenContent(
        state = state,
        actionError = actionError,
        imagePicker = imagePicker,
        onIntent = component::onIntent,
        onBack = onBack,
        onDismissError = { actionError = null },
    )
}

@Composable
internal fun ScannerScreenContent(
    state: ScannerComponent.State,
    actionError: String?,
    imagePicker: ImagePicker?,
    onIntent: (ScannerComponent.Intent) -> Unit,
    onBack: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
        ) {
            DetailTopBar(title = "Сканер документов", onBack = onBack)
            ActionErrorBar(error = actionError, onDismiss = onDismissError)
            ScannerPageList(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            ScannerFooter(state = state, imagePicker = imagePicker, onIntent = onIntent)
        }

        if (state.isEditing) {
            val page = state.editingPage ?: return@Box
            ImageEditorOverlay(
                page = page,
                onUpdateRotation = { onIntent(ScannerComponent.Intent.Editor.UpdateRotation(it)) },
                onDone = { onIntent(ScannerComponent.Intent.Editor.Close) },
            )
        }
    }
}

@Composable
private fun ScannerPageList(
    state: ScannerComponent.State,
    onIntent: (ScannerComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item { ScanHeroCard() }
        item { FileNameField(state.fileName, onIntent) }

        if (state.pages.isEmpty()) {
            item { EmptyPagesCard() }
        } else {
            itemsIndexed(state.pages, key = { _, page -> page.id }) { index, page ->
                PageTile(page, index, index == 0, index == state.pages.lastIndex, onIntent)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ScanHeroCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.AddAPhoto, null, tint = AppTheme.colors.accent, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Сканирование документов",
            color = AppTheme.colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Сфотографируйте или выберите изображения из галереи. Отредактируйте поворот и сохраните как PDF.",
            color = AppTheme.colors.textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FileNameField(
    fileName: String,
    onIntent: (ScannerComponent.Intent) -> Unit,
) {
    OutlinedTextField(
        value = fileName,
        onValueChange = { onIntent(ScannerComponent.Intent.Settings.UpdateFileName(it)) },
        label = { Text("Имя файла") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppTheme.colors.textPrimary,
            unfocusedTextColor = AppTheme.colors.textPrimary,
            focusedBorderColor = AppTheme.colors.accent,
            unfocusedBorderColor = AppTheme.colors.textSecondary.copy(alpha = 0.3f),
            focusedLabelColor = AppTheme.colors.accent,
            unfocusedLabelColor = AppTheme.colors.textSecondary,
            cursorColor = AppTheme.colors.accent,
        ),
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun EmptyPagesCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Нет страниц", color = AppTheme.colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Добавьте изображения с помощью камеры или галереи",
            color = AppTheme.colors.textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PageTile(
    page: ScannerComponent.ScanPage,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onIntent: (ScannerComponent.Intent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.surface)
            .clickable { onIntent(ScannerComponent.Intent.Editor.Open(index)) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PageThumbnail(page, index)
        Spacer(modifier = Modifier.width(12.dp))
        PageInfo(page, index, modifier = Modifier.weight(1f))
        PageActions(index, isFirst, isLast, onIntent)
    }
}

@Composable
private fun PageThumbnail(
    page: ScannerComponent.ScanPage,
    index: Int,
) {
    val imageBitmap = remember(page.id, page.imageBytes) { decodeImageBitmap(page.imageBytes) }
    Box(
        modifier = Modifier
            .size(width = 60.dp, height = 78.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Страница ${index + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = page.rotationDegrees },
            )
        } else {
            Text("IMG", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PageInfo(
    page: ScannerComponent.ScanPage,
    index: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            "Страница ${index + 1}",
            color = AppTheme.colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(formatSizeBytes(page.imageBytes.size.toLong()), color = AppTheme.colors.textSecondary, fontSize = 12.sp)
        if (page.rotationDegrees != 0f) {
            Text(
                "${page.rotationDegrees.toInt()}°",
                color = AppTheme.colors.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PageActions(
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onIntent: (ScannerComponent.Intent) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            ReorderButton(Icons.Default.ArrowUpward, "Вверх", !isFirst) {
                onIntent(ScannerComponent.Intent.Page.MoveUp(index))
            }
            ReorderButton(Icons.Default.ArrowDownward, "Вниз", !isLast) {
                onIntent(ScannerComponent.Intent.Page.MoveDown(index))
            }
        }
        Row {
            IconButton(
                onClick = { onIntent(ScannerComponent.Intent.Editor.Open(index)) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Default.Edit,
                    "Редактировать",
                    tint = AppTheme.colors.accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(
                onClick = { onIntent(ScannerComponent.Intent.Page.Remove(index)) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(Icons.Default.Delete, "Удалить", tint = AppTheme.colors.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ReorderButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (enabled) AppTheme.colors.textSecondary else AppTheme.colors.textSecondary.copy(alpha = 0.3f)
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(32.dp)) {
        Icon(icon, description, tint = tint, modifier = Modifier.size(18.dp))
    }
}
