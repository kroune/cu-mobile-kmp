package io.github.kroune.cumobile.presentation.longread.ui.questions

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.EvaluationStrategy
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.LongreadMaterialContent
import io.github.kroune.cumobile.data.model.QuestionResult
import io.github.kroune.cumobile.data.model.QuizAnswer
import io.github.kroune.cumobile.data.model.QuizAnswerResult
import io.github.kroune.cumobile.data.model.QuizAttempt
import io.github.kroune.cumobile.data.model.QuizOption
import io.github.kroune.cumobile.data.model.QuizQuestion
import io.github.kroune.cumobile.data.model.QuizQuestionContent
import io.github.kroune.cumobile.data.model.QuizQuestionType
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskDetailsExercise
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.longread.component.questions.QuestionsMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.questions.QuestionsMaterialComponent.QuizPhase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.JsonPrimitive

private val previewQuizMaterial = LongreadMaterial(
    id = "q1",
    discriminator = LongreadMaterial.Discriminator.Questions,
    content = LongreadMaterialContent(name = "Тест: Основы алгоритмов"),
)

private val previewQuestions = persistentListOf(
    QuizQuestion(
        id = "q1",
        type = QuizQuestionType.SingleChoice,
        score = 2.0,
        content = QuizQuestionContent(description = "Какова сложность бинарного поиска?"),
        options = listOf(
            QuizOption(id = "a", text = "O(n)"),
            QuizOption(id = "b", text = "O(log n)"),
            QuizOption(id = "c", text = "O(n²)"),
            QuizOption(id = "d", text = "O(1)"),
        ),
    ),
    QuizQuestion(
        id = "q2",
        type = QuizQuestionType.MultipleChoice,
        score = 3.0,
        content = QuizQuestionContent(
            description = "Выберите стабильные алгоритмы сортировки:",
        ),
        options = listOf(
            QuizOption(id = "a", text = "Сортировка слиянием"),
            QuizOption(id = "b", text = "Быстрая сортировка"),
            QuizOption(id = "c", text = "Сортировка вставками"),
            QuizOption(id = "d", text = "Сортировка выбором"),
        ),
    ),
    QuizQuestion(
        id = "q3",
        type = QuizQuestionType.NumberMatch,
        score = 1.0,
        content = QuizQuestionContent(description = "Сколько сравнений в худшем случае для сортировки пузырьком массива из 5 элементов?"),
    ),
    QuizQuestion(
        id = "q4",
        type = QuizQuestionType.StringMatch,
        score = 1.0,
        content = QuizQuestionContent(description = "Назовите структуру данных LIFO (на английском):"),
    ),
    QuizQuestion(
        id = "q5",
        type = QuizQuestionType.OpenText,
        score = 3.0,
        content = QuizQuestionContent(description = "Объясните разницу между стеком и очередью."),
    ),
)

private val previewTaskDetails = ContentState.Success(
    TaskDetails(
        id = "t1",
        state = TaskState.InProgress,
        exercise = TaskDetailsExercise(
            id = "ex1",
            name = "Тест: Основы алгоритмов",
            type = "Questions",
            timer = "00:30:00",
            maxScore = 10.0,
        ),
        quizSessionId = "s1",
        currentAttemptId = "a1",
    ),
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
                taskState = TaskState.Backlog,
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
                taskState = TaskState.Backlog,
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
                taskState = TaskState.InProgress,
                questions = previewQuestions,
                answers = persistentMapOf(
                    "q1" to QuizAnswer.SingleChoice("b"),
                    "q2" to QuizAnswer.MultipleChoice(setOf("a", "c")),
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
                taskState = TaskState.InProgress,
                questions = previewQuestions,
                answers = persistentMapOf(
                    "q1" to QuizAnswer.SingleChoice("b"),
                    "q2" to QuizAnswer.MultipleChoice(setOf("a", "c")),
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
                taskState = TaskState.InProgress,
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
@Preview
@Composable
private fun PreviewQuizCompletedSuccessDark() {
    CuMobileTheme(darkTheme = true) {
        QuestionsMaterialCard(
            material = previewQuizMaterial,
            state = QuestionsMaterialComponent.State(
                isExpanded = true,
                phase = QuizPhase.Completed,
                taskDetails = ContentState.Success(
                    TaskDetails(
                        id = "t1",
                        state = TaskState.Evaluated,
                        score = 8.0,
                        extraScore = 1.0,
                        scoreSkillLevel = "intermediate",
                        exercise = TaskDetailsExercise(
                            id = "ex1",
                            name = "Тест: Основы алгоритмов",
                            type = "Questions",
                            maxScore = 10.0,
                        ),
                        quizSessionId = "s1",
                        evaluatedAttemptId = "a1",
                    ),
                ),
                taskState = TaskState.Evaluated,
                questions = previewQuestions,
                evaluationStrategy = EvaluationStrategy.Best,
                attemptResults = QuizAttempt(
                    id = "a1",
                    score = 8.0,
                    maxScore = 10.0,
                    answers = listOf(
                        QuizAnswerResult(
                            questionId = "q1",
                            result = QuestionResult.Success,
                            score = 2.0,
                            value = JsonPrimitive("b"),
                        ),
                        QuizAnswerResult(
                            questionId = "q2",
                            result = QuestionResult.PartialSuccess,
                            score = 2.0,
                            recommendation = "Сортировка выбором не является стабильной",
                            value = kotlinx.serialization.json.JsonArray(
                                listOf(JsonPrimitive("a"), JsonPrimitive("c")),
                            ),
                        ),
                        QuizAnswerResult(
                            questionId = "q3",
                            result = QuestionResult.Success,
                            score = 1.0,
                            value = JsonPrimitive(10),
                        ),
                        QuizAnswerResult(
                            questionId = "q4",
                            result = QuestionResult.Fail,
                            score = 0.0,
                            recommendation = "Правильный ответ: stack",
                            value = JsonPrimitive("queue"),
                        ),
                        QuizAnswerResult(
                            questionId = "q5",
                            result = QuestionResult.Review,
                            score = 3.0,
                            value = JsonPrimitive("Стек — LIFO, очередь — FIFO"),
                        ),
                    ),
                ),
                pastAttempts = persistentListOf(
                    QuizAttempt(id = "a1", score = 8.0, maxScore = 10.0),
                ),
            ),
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
                taskDetails = ContentState.Success(
                    TaskDetails(
                        id = "t1",
                        state = TaskState.Evaluated,
                        score = 8.0,
                        exercise = TaskDetailsExercise(
                            id = "ex1",
                            name = "Тест: Основы алгоритмов",
                            type = "Questions",
                            maxScore = 10.0,
                        ),
                        quizSessionId = "s1",
                        evaluatedAttemptId = "a1",
                    ),
                ),
                taskState = TaskState.Evaluated,
                questions = previewQuestions,
                evaluationStrategy = EvaluationStrategy.Best,
                attemptResults = QuizAttempt(
                    id = "a1",
                    score = 8.0,
                    maxScore = 10.0,
                    answers = listOf(
                        QuizAnswerResult(
                            questionId = "q1",
                            result = QuestionResult.Success,
                            score = 2.0,
                            value = JsonPrimitive("b"),
                        ),
                        QuizAnswerResult(
                            questionId = "q2",
                            result = QuestionResult.Fail,
                            score = 0.0,
                            value = kotlinx.serialization.json.JsonArray(
                                listOf(JsonPrimitive("b")),
                            ),
                        ),
                    ),
                ),
                pastAttempts = persistentListOf(
                    QuizAttempt(id = "a1", score = 8.0, maxScore = 10.0),
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
                taskDetails = ContentState.Success(
                    TaskDetails(
                        id = "t1",
                        state = TaskState.InProgress,
                        exercise = TaskDetailsExercise(
                            id = "ex1",
                            name = "Тест: Основы алгоритмов",
                            type = "Questions",
                            maxScore = 10.0,
                        ),
                        quizSessionId = "s1",
                    ),
                ),
                taskState = TaskState.InProgress,
                questions = previewQuestions,
                evaluationStrategy = EvaluationStrategy.Last,
                attemptsLimit = 3,
                canStartNewAttempt = true,
                attemptResults = QuizAttempt(
                    id = "a1",
                    score = 5.0,
                    maxScore = 10.0,
                ),
                pastAttempts = persistentListOf(
                    QuizAttempt(id = "a1", score = 5.0, maxScore = 10.0),
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
                taskState = TaskState.InProgress,
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
                taskState = TaskState.InProgress,
            ),
            onIntent = {},
        )
    }
}

// endregion
