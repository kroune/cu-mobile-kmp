package io.github.kroune.cumobile.presentation.longread.htmlrender

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.decodeImageBitmap
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes

private val logger = KotlinLogging.logger {}

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
