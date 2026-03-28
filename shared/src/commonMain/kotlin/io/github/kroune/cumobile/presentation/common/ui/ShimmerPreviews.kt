package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun PreviewShimmerBoxDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 16.dp)
        }
    }
}

@Preview
@Composable
private fun PreviewShimmerBoxLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 16.dp)
        }
    }
}

@Preview
@Composable
private fun PreviewShimmerCircleDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            ShimmerCircle()
        }
    }
}

@Preview
@Composable
private fun PreviewShimmerCircleLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            ShimmerCircle()
        }
    }
}
