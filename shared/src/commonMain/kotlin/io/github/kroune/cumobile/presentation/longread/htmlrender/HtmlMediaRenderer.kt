package io.github.kroune.cumobile.presentation.longread.htmlrender

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import io.github.kroune.cumobile.presentation.common.ui.AppTheme

/** Renders an inline image from HTML `<img>` tag. */
@Composable
internal fun HtmlImageBlock(
    block: HtmlBlock.ImageBlock,
    modifier: Modifier = Modifier,
) {
    val src = resolveUrl(block.src)

    SubcomposeAsyncImage(
        model = src,
        contentDescription = block.alt,
        contentScale = ContentScale.FillWidth,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        loading = {
            Box(
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
        },
        error = {
            block.alt?.let { alt ->
                Text(
                    text = "[$alt]",
                    color = AppTheme.colors.textSecondary,
                    fontSize = 13.sp,
                )
            }
        },
    )
}
