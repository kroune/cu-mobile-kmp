package io.github.kroune.cumobile.presentation.longread.ui.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.longread.htmlrender.resolveUrl
import io.github.kroune.cumobile.presentation.longread.ui.MediaUrlRow

/** Card for `audio` materials with native playback. */
@Composable
fun AudioMaterialCard(
    material: LongreadMaterial,
    modifier: Modifier = Modifier,
) {
    val audioUrl = material.viewContent ?: return
    val playerState = rememberVideoPlayerState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        material.contentName?.let { name ->
            Text(
                text = name,
                color = AppTheme.colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = {
                    if (playerState.isPlaying) {
                        playerState.pause()
                    } else if (playerState.hasMedia) {
                        playerState.play()
                    } else {
                        playerState.openUri(resolveUrl(audioUrl))
                    }
                },
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = AppTheme.colors.accent,
                )
            }

            if (playerState.hasMedia) {
                Text(
                    text = "${playerState.positionText} / ${playerState.durationText}",
                    color = AppTheme.colors.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
            } else {
                TextButton(
                    onClick = { playerState.openUri(resolveUrl(audioUrl)) },
                ) {
                    Text("Play", color = AppTheme.colors.accent)
                }
            }
        }

        MediaUrlRow(url = resolveUrl(audioUrl))
    }
}
