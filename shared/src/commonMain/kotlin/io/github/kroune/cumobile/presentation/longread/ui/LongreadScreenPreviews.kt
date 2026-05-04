package io.github.kroune.cumobile.presentation.longread.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.mappers.toApiValue
import io.github.kroune.cumobile.domain.model.CommentSenderDomain
import io.github.kroune.cumobile.domain.model.LongreadDiscriminator
import io.github.kroune.cumobile.domain.model.LongreadMaterialDomain
import io.github.kroune.cumobile.domain.model.TaskCommentDomain
import io.github.kroune.cumobile.domain.model.TaskDetailsDomain
import io.github.kroune.cumobile.domain.model.TaskDetailsExerciseDomain
import io.github.kroune.cumobile.domain.model.TaskDetailsSolutionDomain
import io.github.kroune.cumobile.domain.model.TaskEventContentDomain
import io.github.kroune.cumobile.domain.model.TaskEventDomain
import io.github.kroune.cumobile.domain.model.TaskEventScoreDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.PendingAttachmentUi
import io.github.kroune.cumobile.presentation.common.model.UploadStatusUi
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi
import io.github.kroune.cumobile.presentation.common.parseDeadlineInstant
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import io.github.kroune.cumobile.presentation.longread.component.coding.CodingMaterialComponent
import io.github.kroune.cumobile.presentation.longread.ui.coding.CodingMaterialCardContent
import kotlinx.collections.immutable.persistentListOf

private val previewCodingMaterial = LongreadMaterialDomain(
    id = "3",
    discriminator = LongreadDiscriminator.Coding,
    contentName = "ДЗ: Быстрая сортировка",
    taskId = "42",
    estimationMaxScore = 10,
).toUi()

private val previewLongreadSuccessState = LongreadComponent.State(
    isLoading = false,
    title = "Введение в алгоритмы",
    materials = persistentListOf(
        LongreadMaterialDomain(
            id = "1",
            discriminator = LongreadDiscriminator.Markdown,
            contentName = "Введение в алгоритмы",
            viewContentRaw =
                "<h2>Алгоритмы</h2>" +
                    "<p>Алгоритм — это <strong>конечная последовательность</strong> " +
                    "точно определённых действий для решения задач.</p>" +
                    "<p>Подробнее на " +
                    "<a href=\"https://example.com\">example.com</a></p>" +
                    "<pre><code class=\"language-python\">def sort(arr):\n" +
                    "    return sorted(arr)</code></pre>" +
                    "<blockquote><p>Сложность — O(n log n)</p></blockquote>" +
                    "<ul><li>Быстрая сортировка</li><li>Сортировка слиянием</li></ul>",
        ).toUi(),
        LongreadMaterialDomain(
            id = "2",
            discriminator = LongreadDiscriminator.File,
            filename = "lecture_slides.pdf",
            length = 2_500_000,
            version = "v1",
        ).toUi(),
        previewCodingMaterial,
        LongreadMaterialDomain(
            id = "4",
            discriminator = LongreadDiscriminator.Questions,
            contentName = "Тест по теме",
        ).toUi(),
    ),
)

private fun previewTaskDetails(
    status: TaskStatus = TaskStatus.InProgress,
    score: Double? = null,
    solutionUrl: String? = null,
    isLateDaysEnabled: Boolean = false,
    lateDays: Int? = null,
    lateDaysBalance: Int? = null,
): ContentState<io.github.kroune.cumobile.presentation.common.model.TaskDetailsUi> =
    ContentState.Success(
        TaskDetailsDomain(
            id = "42",
            score = score,
            extraScore = null,
            scoreSkillLevel = null,
            status = status,
            submitAt = null,
            isLateDaysEnabled = isLateDaysEnabled,
            lateDays = lateDays,
            deadline = parseDeadlineInstant("2026-04-15T23:59:00Z"),
            startedAt = null,
            attemptStartedAt = null,
            quizSessionId = null,
            currentAttemptId = null,
            evaluatedAttemptId = null,
            lastAttemptId = null,
            exercise = TaskDetailsExerciseDomain(
                id = null,
                name = "Быстрая сортировка",
                type = null,
                timer = null,
                maxScore = 10.0,
                attemptsLimit = null,
                evaluationStrategy = null,
            ),
            solution = solutionUrl?.let {
                TaskDetailsSolutionDomain(
                    solutionUrl = it,
                    attachments = emptyList(),
                    answers = emptyList(),
                )
            },
            studentLateDaysBalance = lateDaysBalance,
        ).toUi(),
    )

private val previewComments = persistentListOf(
    TaskCommentDomain(
        id = "c1",
        content = "Проверьте обработку граничных случаев",
        sender = CommentSenderDomain(
            id = "",
            name = "Иванов А.П.",
            email = "ivanov@cu.ru",
        ),
        createdAt = parseDeadlineInstant("2026-04-10T14:30:00Z"),
        attachments = emptyList(),
        isEditable = false,
        isDeletable = false,
    ).toUi(),
    TaskCommentDomain(
        id = "c2",
        content = "Исправил, пожалуйста посмотрите ещё раз",
        sender = CommentSenderDomain(
            id = "",
            name = "Студент",
            email = "student@cu.ru",
        ),
        createdAt = parseDeadlineInstant("2026-04-11T09:15:00Z"),
        attachments = emptyList(),
        isEditable = true,
        isDeletable = true,
    ).toUi(),
)

private val previewEvents = persistentListOf(
    TaskEventDomain(
        id = "e1",
        occurredOn = parseDeadlineInstant("2026-04-01T10:00:00Z"),
        type = "taskStarted",
        actorEmail = null,
        actorName = "Студент",
        content = TaskEventContentDomain(
            state = TaskStatus.InProgress.toApiValue(),
            score = null,
            estimation = null,
            solution = null,
            reviewer = null,
            reviewers = null,
            task = null,
            name = null,
            lateDays = null,
            deadline = null,
            attached = null,
        ),
    ).toUi(),
    TaskEventDomain(
        id = "e2",
        occurredOn = parseDeadlineInstant("2026-04-05T18:00:00Z"),
        type = "taskSubmitted",
        actorEmail = null,
        actorName = "Студент",
        content = TaskEventContentDomain(
            state = TaskStatus.Review.toApiValue(),
            score = null,
            estimation = null,
            solution = null,
            reviewer = null,
            reviewers = null,
            task = null,
            name = null,
            lateDays = null,
            deadline = null,
            attached = null,
        ),
    ).toUi(),
    TaskEventDomain(
        id = "e3",
        occurredOn = parseDeadlineInstant("2026-04-07T12:00:00Z"),
        type = "taskEvaluated",
        actorEmail = null,
        actorName = "Иванов А.П.",
        content = TaskEventContentDomain(
            state = TaskStatus.Evaluated.toApiValue(),
            score = TaskEventScoreDomain(level = null, value = 8.0),
            estimation = null,
            solution = null,
            reviewer = null,
            reviewers = null,
            task = null,
            name = null,
            lateDays = null,
            deadline = null,
            attached = null,
        ),
    ).toUi(),
)

// region Screen-level previews

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
                isLoading = false,
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
                isLoading = false,
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
private fun PreviewLongreadEmptyMaterialsDark() {
    CuMobileTheme(darkTheme = true) {
        LongreadScreenContent(
            state = LongreadComponent.State(isLoading = false),
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

// endregion

// region CodingMaterialCardContent previews

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardBacklogDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                taskDetails = previewTaskDetails(status = TaskStatus.Backlog),
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardSolutionTabDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "solution",
                taskDetails = previewTaskDetails(status = TaskStatus.InProgress),
                solutionUrl = "https://github.com/student/quicksort",
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardSolutionTabLight() {
    CuMobileTheme(darkTheme = false) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "solution",
                taskDetails = previewTaskDetails(status = TaskStatus.InProgress),
                solutionUrl = "https://github.com/student/quicksort",
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardWithAttachmentsDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "solution",
                taskDetails = previewTaskDetails(status = TaskStatus.InProgress),
                pendingSolutionAttachments = persistentListOf(
                    PendingAttachmentUi(
                        name = "solution.py",
                        size = 4096,
                        status = UploadStatusUi.Uploaded,
                    ),
                    PendingAttachmentUi(
                        name = "tests.py",
                        size = 2048,
                        status = UploadStatusUi.Uploading,
                    ),
                    PendingAttachmentUi(
                        name = "broken.txt",
                        size = 512,
                        status = UploadStatusUi.Failed,
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardEvaluatedDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "solution",
                taskDetails = previewTaskDetails(
                    status = TaskStatus.Evaluated,
                    score = 8.0,
                    solutionUrl = "https://github.com/student/quicksort",
                ),
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardCommentsTabDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "comments",
                taskDetails = previewTaskDetails(status = TaskStatus.InProgress),
                taskComments = ContentState.Success(previewComments),
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardCommentsTabLight() {
    CuMobileTheme(darkTheme = false) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "comments",
                taskDetails = previewTaskDetails(status = TaskStatus.InProgress),
                taskComments = ContentState.Success(previewComments),
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardCommentEditingDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "comments",
                taskDetails = previewTaskDetails(status = TaskStatus.InProgress),
                taskComments = ContentState.Success(previewComments),
                editingCommentId = "c2",
                editCommentText = "Исправил обработку граничных случаев",
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardInfoTabDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "info",
                taskDetails = previewTaskDetails(
                    status = TaskStatus.Evaluated,
                    score = 8.0,
                    isLateDaysEnabled = true,
                    lateDays = 2,
                    lateDaysBalance = 5,
                ),
                taskEvents = ContentState.Success(previewEvents),
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardLateDaysDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "solution",
                taskDetails = previewTaskDetails(
                    status = TaskStatus.InProgress,
                    isLateDaysEnabled = true,
                    lateDays = 3,
                    lateDaysBalance = 4,
                ),
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardSubmittingDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = true,
                selectedTab = "solution",
                taskDetails = previewTaskDetails(status = TaskStatus.InProgress),
                isSubmitting = true,
                solutionUrl = "https://github.com/student/quicksort",
            ),
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewCodingCardCollapsedDark() {
    CuMobileTheme(darkTheme = true) {
        CodingMaterialCardContent(
            material = previewCodingMaterial,
            state = CodingMaterialComponent.State(
                isExpanded = false,
                taskDetails = previewTaskDetails(status = TaskStatus.InProgress),
            ),
            onIntent = {},
        )
    }
}

// endregion
