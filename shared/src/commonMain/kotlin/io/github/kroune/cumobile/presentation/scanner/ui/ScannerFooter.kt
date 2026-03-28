package io.github.kroune.cumobile.presentation.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.ImagePicker
import io.github.kroune.cumobile.presentation.scanner.ScannerComponent

@Composable
internal fun ScannerFooter(
    state: ScannerComponent.State,
    imagePicker: ImagePicker?,
    onIntent: (ScannerComponent.Intent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CaptureButtons(imagePicker)
        CompressionToggle(state.compressImages) { onIntent(ScannerComponent.Intent.Settings.SetCompression(it)) }
        SaveButton(state) { onIntent(ScannerComponent.Intent.SavePdf) }
    }
}

@Composable
private fun CaptureButtons(imagePicker: ImagePicker?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = { imagePicker?.launchCamera() },
            modifier = Modifier.weight(1f),
            enabled = imagePicker != null,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.accent),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Камера", fontSize = 14.sp)
        }
        OutlinedButton(
            onClick = { imagePicker?.launchGallery() },
            modifier = Modifier.weight(1f),
            enabled = imagePicker != null,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.accent),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Галерея", fontSize = 14.sp)
        }
    }
}

@Composable
private fun CompressionToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("Сжать изображения", color = AppTheme.colors.textPrimary, fontSize = 14.sp)
            Text("Уменьшает размер PDF", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppTheme.colors.accent,
                checkedTrackColor = AppTheme.colors.accent.copy(alpha = 0.3f),
            ),
        )
    }
}

@Composable
private fun SaveButton(
    state: ScannerComponent.State,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = state.canSave,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.accent,
            disabledContainerColor = AppTheme.colors.textSecondary.copy(alpha = 0.3f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        if (state.isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = AppTheme.colors.background,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            if (state.isSaving) "Сохранение..." else "Сохранить PDF",
            color = AppTheme.colors.background,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
