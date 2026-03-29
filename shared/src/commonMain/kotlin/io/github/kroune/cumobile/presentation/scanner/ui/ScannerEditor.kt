package io.github.kroune.cumobile.presentation.scanner.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.scanner.ScannerComponent

@Composable
internal fun ImageEditorOverlay(
    page: ScannerComponent.ScanPage,
    onUpdateRotation: (Float) -> Unit,
    onDone: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f))) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorTopBar(onDone)
            EditorImagePreview(
                imageBytes = page.imageBytes,
                rotationDegrees = page.rotationDegrees,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            RotationControls(page.rotationDegrees, onUpdateRotation)
            EditorDoneButton(onDone)
        }
    }
}

@Composable
private fun EditorTopBar(onDone: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Редактирование",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp),
        )
        IconButton(onClick = onDone) { Icon(Icons.Default.Close, "Закрыть", tint = Color.White) }
    }
}

@Composable
private fun EditorImagePreview(
    imageBytes: ByteArray,
    rotationDegrees: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = imageBytes,
            contentDescription = "Предпросмотр",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotationDegrees },
        )
    }
}

@Composable
private fun RotationControls(
    rotationDegrees: Float,
    onUpdateRotation: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "${rotationDegrees.toInt()}°",
            color = if (rotationDegrees != 0f) AppTheme.colors.accent else Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = rotationDegrees,
            onValueChange = onUpdateRotation,
            valueRange = -180f..180f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = AppTheme.colors.accent,
                activeTrackColor = AppTheme.colors.accent,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        QuickRotationButtons(onUpdateRotation)
    }
}

@Composable
private fun QuickRotationButtons(onUpdateRotation: (Float) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        QuickRotateButton(
            Icons.AutoMirrored.Filled.RotateLeft,
            "-90°",
        ) { onUpdateRotation(-RotationStep) }
        TextButton(onClick = { onUpdateRotation(0f) }) {
            Text("Сброс", color = Color.White, fontSize = 14.sp)
        }
        QuickRotateButton(
            Icons.AutoMirrored.Filled.RotateRight,
            "+90°",
        ) { onUpdateRotation(RotationStep) }
    }
}

@Composable
private fun QuickRotateButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
        ) {
            Icon(icon, label, tint = Color.White)
        }
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
private fun EditorDoneButton(onDone: () -> Unit) {
    Button(
        onClick = onDone,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accent),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text("Готово", color = AppTheme.colors.background, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

private const val RotationStep = 90f
