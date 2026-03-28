package io.github.kroune.cumobile.presentation.longread.htmlrender.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.kroune.cumobile.presentation.common.decodeImageBitmap
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.longread.htmlrender.HtmlBlock
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes

private val logger = KotlinLogging.logger {}

private const val VideoAspectRatioWidth = 16f
private const val VideoAspectRatioHeight = 9f

/** Renders an inline image from HTML `<img>` tag. */
@Composable
internal fun HtmlImageBlock(
    block: HtmlBlock.ImageBlock,
    modifier: Modifier = Modifier,
) {
    val src = resolveUrl(block.src)
    var bitmap by remember(src) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(src) { mutableStateOf(true) }
    var error by remember(src) { mutableStateOf(false) }

    LaunchedEffect(src) {
        loading = true
        error = false
        bitmap = loadImageBitmap(src)
        loading = false
        error = bitmap == null
    }

    when {
        loading -> Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AppTheme.colors.accent,
            )
        }
        bitmap != null -> bitmap?.let { loadedBitmap ->
            Image(
                bitmap = loadedBitmap,
                contentDescription = block.alt,
                contentScale = ContentScale.FillWidth,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
        else -> block.alt?.let { alt ->
            Text(
                text = "[$alt]",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
                modifier = modifier,
            )
        }
    }
}

/** Card for standalone `image` discriminator materials. */
@Composable
fun ImageMaterialCard(
    material: LongreadMaterial,
    modifier: Modifier = Modifier,
) {
    val url = material.viewContent?.let { resolveUrl(it) }
        ?: material.filename?.let { resolveUrl(it) }
    if (url == null) return

    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(url) { mutableStateOf(true) }

    LaunchedEffect(url) {
        loading = true
        bitmap = loadImageBitmap(url)
        loading = false
    }

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
        when {
            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppTheme.colors.accent,
                )
            }
            bitmap != null -> bitmap?.let { loadedBitmap ->
                Image(
                    bitmap = loadedBitmap,
                    contentDescription = material.contentName,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                )
            }
            else -> Text(
                text = "Failed to load image",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
            )
        }
    }
}

/** Card for `video` and `videoPlatform` materials with native playback. */
@Composable
fun VideoMaterialCard(
    material: LongreadMaterial,
    modifier: Modifier = Modifier,
) {
    val videoUrl = material.viewContent ?: return
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(VideoAspectRatioWidth / VideoAspectRatioHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(AppTheme.colors.codeBlockBackground),
            contentAlignment = Alignment.Center,
        ) {
            VideoPlayerSurface(
                playerState = playerState,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(
                onClick = {
                    if (playerState.isPlaying) {
                        playerState.pause()
                    } else if (playerState.hasMedia) {
                        playerState.play()
                    } else {
                        playerState.openUri(resolveUrl(videoUrl))
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
                    fontSize = 12.sp,
                )
            }
        }

        MediaUrlRow(url = resolveUrl(videoUrl))
    }
}

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

/** Row displaying a URL with copy-to-clipboard and open-in-browser actions. */
@Composable
fun MediaUrlRow(
    url: String,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.codeBlockBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = url,
            color = AppTheme.colors.textSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { clipboardManager.setText(AnnotatedString(url)) },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy URL",
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
        IconButton(
            onClick = { uriHandler.openUri(url) },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.OpenInBrowser,
                contentDescription = "Open in browser",
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private suspend fun loadImageBitmap(url: String): ImageBitmap? =
    runCatchingCancellable {
        val bytes = HttpClient().use { client ->
            client.get(url).readRawBytes()
        }
        decodeImageBitmap(bytes)
    }.getOrElse { e ->
        logger.warn(e) { "Failed to load image: $url" }
        null
    }
