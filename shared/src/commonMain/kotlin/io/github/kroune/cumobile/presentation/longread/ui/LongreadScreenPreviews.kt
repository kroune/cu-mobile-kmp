package io.github.kroune.cumobile.presentation.longread.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.CommentSender
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.LongreadMaterialContent
import io.github.kroune.cumobile.data.model.MaterialEstimation
import io.github.kroune.cumobile.data.model.PendingAttachment
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskDetailsExercise
import io.github.kroune.cumobile.data.model.TaskDetailsSolution
import io.github.kroune.cumobile.data.model.TaskDetailsStudent
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.kroune.cumobile.data.model.TaskEventContent
import io.github.kroune.cumobile.data.model.TaskEventScore
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.data.model.UploadStatus
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

private val previewCodingMaterial = LongreadMaterial(
    id = "3",
    discriminator = "coding",
    content = LongreadMaterialContent(name = "ДЗ: Быстрая сортировка"),
    taskId = "42",
    estimation = MaterialEstimation(maxScore = 10),
)

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
        previewCodingMaterial,
        LongreadMaterial(
            id = "4",
            discriminator = "questions",
            content = LongreadMaterialContent(name = "Тест по теме"),
        ),
    ),
)

private fun previewTaskDetails(
    state: String = TaskState.InProgress,
    score: Double? = null,
    solutionUrl: String? = null,
    isLateDaysEnabled: Boolean = false,
    lateDays: Int? = null,
    lateDaysBalance: Int? = null,
) = TaskDetails(
    id = "42",
    state = state,
    score = score,
    deadline = "2026-04-15T23:59:00Z",
    exercise = TaskDetailsExercise(name = "Быстрая сортировка", maxScore = 10),
    solution = solutionUrl?.let { TaskDetailsSolution(solutionUrl = it) },
    isLateDaysEnabled = isLateDaysEnabled,
    lateDays = lateDays,
    student = lateDaysBalance?.let { TaskDetailsStudent(lateDaysBalance = it) },
)

private val previewComments = persistentListOf(
    TaskComment(
        id = "c1",
        content = "Проверьте обработку граничных случаев",
        sender = CommentSender(name = "Иванов А.П.", email = "ivanov@cu.ru"),
        createdAt = "2026-04-10T14:30:00Z",
        isEditable = false,
        isDeletable = false,
    ),
    TaskComment(
        id = "c2",
        content = "Исправил, пожалуйста посмотрите ещё раз",
        sender = CommentSender(name = "Студент", email = "student@cu.ru"),
        createdAt = "2026-04-11T09:15:00Z",
        isEditable = true,
        isDeletable = true,
    ),
)

private val previewEvents = persistentListOf(
    TaskEvent(
        id = "e1",
        occurredOn = "2026-04-01T10:00:00Z",
        type = "taskStarted",
        actorName = "Студент",
        content = TaskEventContent(state = TaskState.InProgress),
    ),
    TaskEvent(
        id = "e2",
        occurredOn = "2026-04-05T18:00:00Z",
        type = "taskSubmitted",
        actorName = "Студент",
        content = TaskEventContent(state = TaskState.Review),
    ),
    TaskEvent(
        id = "e3",
        occurredOn = "2026-04-07T12:00:00Z",
        type = "taskEvaluated",
        actorName = "Иванов А.П.",
        content = TaskEventContent(
            state = TaskState.Evaluated,
            score = TaskEventScore(value = 8.0),
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

@Preview
@Composable
private fun PreviewLongreadEmptyMaterialsDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = LongreadComponent.State(),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskBacklogDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(state = TaskState.Backlog),
                ),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskSolutionTabDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "solution",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(state = TaskState.InProgress),
                ),
                solutionUrl = "https://github.com/student/quicksort",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskSolutionTabLight() {
    CuMobileTheme(darkTheme = false) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "solution",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(state = TaskState.InProgress),
                ),
                solutionUrl = "https://github.com/student/quicksort",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskSolutionWithAttachmentsDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "solution",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(state = TaskState.InProgress),
                ),
                pendingSolutionAttachments = persistentListOf(
                    PendingAttachment(
                        name = "solution.py",
                        size = 4096,
                        status = UploadStatus.Uploaded,
                    ),
                    PendingAttachment(
                        name = "tests.py",
                        size = 2048,
                        status = UploadStatus.Uploading,
                    ),
                    PendingAttachment(
                        name = "broken.txt",
                        size = 512,
                        status = UploadStatus.Failed,
                    ),
                ),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskHasSolutionDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "solution",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(
                        state = TaskState.Review,
                        solutionUrl = "https://github.com/student/quicksort",
                    ),
                ),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskEvaluatedDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "solution",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(
                        state = TaskState.Evaluated,
                        score = 8.0,
                        solutionUrl = "https://github.com/student/quicksort",
                    ),
                ),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskRevisionDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "solution",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(
                        state = TaskState.Revision,
                        score = 4.0,
                        solutionUrl = "https://github.com/student/quicksort-v1",
                    ),
                ),
                solutionUrl = "https://github.com/student/quicksort-v2",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskCommentsTabDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "comments",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(state = TaskState.InProgress),
                ),
                taskComments = previewComments,
                commentText = "",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskCommentsTabLight() {
    CuMobileTheme(darkTheme = false) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "comments",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(state = TaskState.InProgress),
                ),
                taskComments = previewComments,
                commentText = "",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskCommentEditingDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "comments",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(state = TaskState.InProgress),
                ),
                taskComments = previewComments,
                editingCommentId = "c2",
                editCommentText = "Исправил обработку граничных случаев",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskCommentsEmptyDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "comments",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(state = TaskState.InProgress),
                ),
                taskComments = persistentListOf(),
                commentText = "Вопрос по заданию...",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskInfoTabDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "info",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(
                        state = TaskState.Evaluated,
                        score = 8.0,
                        isLateDaysEnabled = true,
                        lateDays = 2,
                        lateDaysBalance = 5,
                    ),
                ),
                taskEvents = previewEvents,
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskInfoTabLight() {
    CuMobileTheme(darkTheme = false) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "info",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(
                        state = TaskState.Evaluated,
                        score = 8.0,
                        isLateDaysEnabled = true,
                        lateDays = 2,
                        lateDaysBalance = 5,
                    ),
                ),
                taskEvents = previewEvents,
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskLateDaysDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "solution",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(
                        state = TaskState.InProgress,
                        isLateDaysEnabled = true,
                        lateDays = 3,
                        lateDaysBalance = 4,
                    ),
                ),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskSubmittingDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "solution",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(state = TaskState.InProgress),
                ),
                isSubmitting = true,
                solutionUrl = "https://github.com/student/quicksort",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongreadTaskFailedDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = previewLongreadSuccessState.copy(
                activeTaskId = "42",
                selectedTaskTab = "solution",
                taskDetails = persistentMapOf(
                    "42" to previewTaskDetails(
                        state = TaskState.Failed,
                        score = 2.0,
                        solutionUrl = "https://github.com/student/quicksort",
                    ),
                ),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}
