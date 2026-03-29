package io.github.kroune.cumobile.presentation.longread.ui.file

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme

@Preview
@Composable
private fun PreviewFileMaterialCard() {
    CuMobileTheme {
        FileMaterialCard(
            material = LongreadMaterial(
                discriminator = LongreadMaterial.Discriminator.File,
                filename = "lecture_notes.pdf",
                version = "1",
                length = 2_048_000,
            ),
            onDownload = {},
        )
    }
}
