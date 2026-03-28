package io.github.kroune.cumobile.presentation.longread.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.LongreadMaterialContent
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import kotlinx.collections.immutable.persistentListOf

private val previewLongreadSuccessState = LongreadComponent.State(
    materials = persistentListOf(
        LongreadMaterial(
            id = "1",
            discriminator = "markdown",
            content = LongreadMaterialContent(name = "Введение в алгоритмы"),
            viewContentRaw = kotlinx.serialization.json.JsonPrimitive(
                "<h2>Алгоритмы</h2>" +
                    "<p>Алгоритм — это <strong>конечная последовательность</strong> " +
                    "точно определённых действий для решения задач.</p>" +
                    "<p>Подробнее на " +
                    "<a href=\"https://example.com\">example.com</a></p>" +
                    "<pre><code class=\"language-python\">def sort(arr):\n" +
                    "    return sorted(arr)</code></pre>" +
                    "<blockquote><p>Сложность — O(n log n)</p></blockquote>" +
                    "<ul><li>Быстрая сортировка</li><li>Сортировка слиянием</li></ul>",
            ),
        ),
        LongreadMaterial(
            id = "2",
            discriminator = "file",
            filename = "lecture_slides.pdf",
            length = 2_500_000,
            version = "v1",
        ),
        LongreadMaterial(
            id = "3",
            discriminator = "coding",
            content = LongreadMaterialContent(name = "ДЗ: Быстрая сортировка"),
            taskId = "42",
        ),
        LongreadMaterial(
            id = "4",
            discriminator = "questions",
            content = LongreadMaterialContent(name = "Тест по теме"),
        ),
    ),
)

@Preview
@Composable
private fun PreviewLongreadScreenSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = LongreadComponent.State(isLoading = true),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadScreenSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        LongreadScreenContent(
            state = LongreadComponent.State(isLoading = true),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadLoadErrorDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = LongreadComponent.State(
                error = "Не удалось загрузить материалы",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadLoadErrorLight() {
    CuMobileTheme(darkTheme = false) {
        LongreadScreenContent(
            state = LongreadComponent.State(
                error = "Не удалось загрузить материалы",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadActionErrorDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = LongreadComponent.State(
                materials = persistentListOf(
                    LongreadMaterial(
                        id = "1",
                        name = "Тестовый материал",
                        discriminator = "markdown",
                    ),
                ),
            ),
            actionError = "Не удалось отправить решение",
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadActionErrorLight() {
    CuMobileTheme(darkTheme = false) {
        LongreadScreenContent(
            state = LongreadComponent.State(
                materials = persistentListOf(
                    LongreadMaterial(
                        id = "1",
                        name = "Тестовый материал",
                        discriminator = "markdown",
                    ),
                ),
            ),
            actionError = "Не удалось отправить решение",
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = LongreadComponent.State(isLoading = true),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadSuccessDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState,
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadSuccessLight() {
    CuMobileTheme(darkTheme = false) {
        LongreadScreenContent(
            state = previewLongreadSuccessState,
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadSearchDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                isSearchVisible = true,
                searchQuery = "алгоритм",
                searchMatchCount = 2,
                currentMatchIndex = 0,
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadSearchNoMatchesDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                isSearchVisible = true,
                searchQuery = "несуществующий",
                searchMatchCount = 0,
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}
