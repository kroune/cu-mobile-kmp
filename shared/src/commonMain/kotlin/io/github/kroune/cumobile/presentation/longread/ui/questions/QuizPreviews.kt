package io.github.kroune.cumobile.presentation.longread.ui.questions

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.domain.model.AnswerValue
import io.github.kroune.cumobile.domain.model.LongreadDiscriminator
import io.github.kroune.cumobile.domain.model.LongreadMaterialDomain
import io.github.kroune.cumobile.domain.model.QuestionResult
import io.github.kroune.cumobile.domain.model.QuizAnswer
import io.github.kroune.cumobile.domain.model.QuizAnswerResultDomain
import io.github.kroune.cumobile.domain.model.QuizAttemptDomain
import io.github.kroune.cumobile.domain.model.QuizOptionDomain
import io.github.kroune.cumobile.domain.model.QuizQuestionDomain
import io.github.kroune.cumobile.domain.model.QuizQuestionType
import io.github.kroune.cumobile.domain.model.TaskDetailsDomain
import io.github.kroune.cumobile.domain.model.TaskDetailsExerciseDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi
import io.github.kroune.cumobile.presentation.common.model.toStatusStyle
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.longread.component.questions.QuestionsMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.questions.QuestionsMaterialComponent.QuizPhase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

private val previewQuizMaterial = LongreadMaterialDomain(
    id = "q1",
    discriminator = LongreadDiscriminator.Questions,
    contentName = "Тест: Основы алгоритмов",
).toUi()

private val previewQuestions = persistentListOf(
    QuizQuestionDomain(
        id = "q1",
        type = QuizQuestionType.SingleChoice,
        score = 2.0,
        description = "Какова сложность бинарного поиска?",
        recommendation = null,
        options = listOf(
            QuizOptionDomain(id = "a", text = "O(n)"),
            QuizOptionDomain(id = "b", text = "O(log n)"),
            QuizOptionDomain(id = "c", text = "O(n²)"),
            QuizOptionDomain(id = "d", text = "O(1)"),
        ),
    ).toUi(),
    QuizQuestionDomain(
        id = "q2",
        type = QuizQuestionType.MultipleChoice,
        score = 3.0,
        description = "Выберите стабильные алгоритмы сортировки:",
        recommendation = null,
        options = listOf(
            QuizOptionDomain(id = "a", text = "Сортировка слиянием"),
            QuizOptionDomain(id = "b", text = "Быстрая сортировка"),
            QuizOptionDomain(id = "c", text = "Сортировка вставками"),
            QuizOptionDomain(id = "d", text = "Сортировка выбором"),
        ),
    ).toUi(),
    QuizQuestionDomain(
        id = "q3",
        type = QuizQuestionType.NumberMatch,
        score = 1.0,
        description = "Сколько сравнений в худшем случае для сортировки пузырьком массива из 5 элементов?",
        recommendation = null,
        options = emptyList(),
    ).toUi(),
    QuizQuestionDomain(
        id = "q4",
        type = QuizQuestionType.StringMatch,
        score = 1.0,
        description = "Назовите структуру данных LIFO (на английском):",
        recommendation = null,
        options = emptyList(),
    ).toUi(),
    QuizQuestionDomain(
        id = "q5",
        type = QuizQuestionType.OpenText,
        score = 3.0,
        description = "Объясните разницу между стеком и очередью.",
        recommendation = null,
        options = emptyList(),
    ).toUi(),
)

private fun previewQuizTaskDetails(
    status: TaskStatus = TaskStatus.InProgress,
    score: Double? = null,
    extraScore: Double? = null,
    scoreSkillLevel: String? = null,
    quizSessionId: String? = null,
    currentAttemptId: String? = null,
    evaluatedAttemptId: String? = null,
    exerciseTimer: String? = null,
    exerciseMaxScore: Double? = null,
) =
    ContentState.Success(
        TaskDetailsDomain(
            id = "t1",
            score = score,
            extraScore = extraScore,
            scoreSkillLevel = scoreSkillLevel,
            status = status,
            submitAt = null,
            isLateDaysEnabled = false,
            lateDays = null,
            deadline = null,
            startedAt = null,
            attemptStartedAt = null,
            quizSessionId = quizSessionId,
            currentAttemptId = currentAttemptId,
            evaluatedAttemptId = evaluatedAttemptId,
            lastAttemptId = null,
            exercise = TaskDetailsExerciseDomain(
                id = "ex1",
                name = "Тест: Основы алгоритмов",
                type = "Questions",
                timer = exerciseTimer,
                maxScore = exerciseMaxScore,
                attemptsLimit = null,
                evaluationStrategy = null,
            ),
            solution = null,
            studentLateDaysBalance = null,
        ).toUi(),
    )

private val previewTaskDetails = previewQuizTaskDetails(
    quizSessionId = "s1",
    currentAttemptId = "a1",
    exerciseTimer = "00:30:00",
    exerciseMaxScore = 10.0,
)

// region NotStarted

@Preview
@Composable
private fun PreviewQuizNotStartedDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.NotStarted,
                taskDetails = previewTaskDetails,
                taskStatusStyle = TaskStatus.Backlog.toStatusStyle(),
                attemptsLimit = 3,
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewQuizNotStartedLight() {
    CuMobileTheme(darkTheme = false) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.NotStarted,
                taskDetails = previewTaskDetails,
                taskStatusStyle = TaskStatus.Backlog.toStatusStyle(),
                attemptsLimit = 3,
            ),
            onIntent = {},
        )
    }
}

// endregion

// region InProgress

@Preview
@Composable
private fun PreviewQuizInProgressDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.InProgress,
                taskDetails = previewTaskDetails,
                taskStatusStyle = TaskStatus.InProgress.toStatusStyle(),
                questions = previewQuestions,
                answers = persistentMapOf(
                    "q1" to QuizAnswer.SingleChoice("b").toUi(),
                    "q2" to QuizAnswer.MultipleChoice(setOf("a", "c")).toUi(),
                ),
                sessionId = "s1",
                attemptId = "a1",
                timerTotalSeconds = 1800,
                timerRemainingSeconds = 1200,
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewQuizInProgressLight() {
    CuMobileTheme(darkTheme = false) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.InProgress,
                taskDetails = previewTaskDetails,
                taskStatusStyle = TaskStatus.InProgress.toStatusStyle(),
                questions = previewQuestions,
                answers = persistentMapOf(
                    "q1" to QuizAnswer.SingleChoice("b").toUi(),
                    "q2" to QuizAnswer.MultipleChoice(setOf("a", "c")).toUi(),
                ),
                sessionId = "s1",
                attemptId = "a1",
                timerTotalSeconds = 1800,
                timerRemainingSeconds = 1200,
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewQuizTimerLowDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.InProgress,
                taskDetails = previewTaskDetails,
                taskStatusStyle = TaskStatus.InProgress.toStatusStyle(),
                questions = previewQuestions,
                sessionId = "s1",
                attemptId = "a1",
                timerTotalSeconds = 1800,
                timerRemainingSeconds = 120,
            ),
            onIntent = {},
        )
    }
}

// endregion

// region Completed

@Suppress("MagicNumber")
private val previewCompletedSuccessState = QuestionsMaterialComponent.State(
    isExpanded = true,
    phase = QuizPhase.Completed,
    taskDetails = previewQuizTaskDetails(
        status = TaskStatus.Evaluated,
        score = 8.0,
        extraScore = 1.0,
        scoreSkillLevel = "intermediate",
        quizSessionId = "s1",
        evaluatedAttemptId = "a1",
        exerciseMaxScore = 10.0,
    ),
    taskStatusStyle = TaskStatus.Evaluated.toStatusStyle(),
    questions = previewQuestions,
    evaluationStrategy = "Best",
    attemptResults = QuizAttemptDomain(
        id = "a1",
        score = 8.0,
        maxScore = 10.0,
        answers = listOf(
            QuizAnswerResultDomain(
                questionId = "q1",
                result = QuestionResult.Success,
                score = 2.0,
                recommendation = null,
                answerValue = AnswerValue.Text("b"),
            ),
            QuizAnswerResultDomain(
                questionId = "q2",
                result = QuestionResult.PartialSuccess,
                score = 2.0,
                recommendation = "Сортировка выбором не является стабильной",
                answerValue = AnswerValue.Choices(listOf("a", "c")),
            ),
            QuizAnswerResultDomain(
                questionId = "q3",
                result = QuestionResult.Success,
                score = 1.0,
                recommendation = null,
                answerValue = AnswerValue.Text("10"),
            ),
            QuizAnswerResultDomain(
                questionId = "q4",
                result = QuestionResult.Fail,
                score = 0.0,
                recommendation = "Правильный ответ: stack",
                answerValue = AnswerValue.Text("queue"),
            ),
            QuizAnswerResultDomain(
                questionId = "q5",
                result = QuestionResult.Review,
                score = 3.0,
                recommendation = null,
                answerValue = AnswerValue.Text("Стек — LIFO, очередь — FIFO"),
            ),
        ),
    ).toUi(),
    pastAttempts = persistentListOf(
        QuizAttemptDomain(id = "a1", score = 8.0, maxScore = 10.0, answers = emptyList()).toUi(),
    ),
)

@Preview
@Composable
private fun PreviewQuizCompletedSuccessDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = previewCompletedSuccessState,
            onIntent = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewQuizCompletedSuccessLight() {
    CuMobileTheme(darkTheme = false) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.Completed,
                taskDetails = previewQuizTaskDetails(
                    status = TaskStatus.Evaluated,
                    score = 8.0,
                    quizSessionId = "s1",
                    evaluatedAttemptId = "a1",
                    exerciseMaxScore = 10.0,
                ),
                taskStatusStyle = TaskStatus.Evaluated.toStatusStyle(),
                questions = previewQuestions,
                evaluationStrategy = "Best",
                attemptResults = QuizAttemptDomain(
                    id = "a1",
                    score = 8.0,
                    maxScore = 10.0,
                    answers = listOf(
                        QuizAnswerResultDomain(
                            questionId = "q1",
                            result = QuestionResult.Success,
                            score = 2.0,
                            recommendation = null,
                            answerValue = AnswerValue.Text("b"),
                        ),
                        QuizAnswerResultDomain(
                            questionId = "q2",
                            result = QuestionResult.Fail,
                            score = 0.0,
                            recommendation = null,
                            answerValue = AnswerValue.Choices(listOf("b")),
                        ),
                    ),
                ).toUi(),
                pastAttempts = persistentListOf(
                    QuizAttemptDomain(
                        id = "a1",
                        score = 8.0,
                        maxScore = 10.0,
                        answers = emptyList(),
                    ).toUi(),
                ),
            ),
            onIntent = {},
        )
    }
}

// endregion

// region Retry available

@Preview
@Composable
private fun PreviewQuizCompletedWithRetryDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.Completed,
                taskDetails = previewQuizTaskDetails(
                    status = TaskStatus.InProgress,
                    quizSessionId = "s1",
                    exerciseMaxScore = 10.0,
                ),
                taskStatusStyle = TaskStatus.InProgress.toStatusStyle(),
                questions = previewQuestions,
                evaluationStrategy = "Last",
                attemptsLimit = 3,
                canStartNewAttempt = true,
                attemptResults = QuizAttemptDomain(
                    id = "a1",
                    score = 5.0,
                    maxScore = 10.0,
                    answers = emptyList(),
                ).toUi(),
                pastAttempts = persistentListOf(
                    QuizAttemptDomain(
                        id = "a1",
                        score = 5.0,
                        maxScore = 10.0,
                        answers = emptyList(),
                    ).toUi(),
                ),
            ),
            onIntent = {},
        )
    }
}

// endregion

// region Collapsed / Loading / Error

@Preview
@Composable
private fun PreviewQuizCollapsedDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = false,
                phase = QuizPhase.InProgress,
                taskDetails = previewTaskDetails,
                taskStatusStyle = TaskStatus.InProgress.toStatusStyle(),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewQuizLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.Loading,
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewQuizErrorDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.Error("Не удалось загрузить задание"),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewQuizCompletingDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.Completing,
                taskStatusStyle = TaskStatus.InProgress.toStatusStyle(),
            ),
            onIntent = {},
        )
    }
}

// endregion
