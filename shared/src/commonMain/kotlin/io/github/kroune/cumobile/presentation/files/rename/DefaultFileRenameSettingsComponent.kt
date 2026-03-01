package io.github.kroune.cumobile.presentation.files.rename

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.local.FileRenameRule
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.FileRenameRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [FileRenameSettingsComponent].
 */
class DefaultFileRenameSettingsComponent(
    componentContext: ComponentContext,
    private val renameRepository: FileRenameRepository,
    private val courseRepository: CourseRepository,
    private val onBack: () -> Unit,
) : FileRenameSettingsComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(FileRenameSettingsComponent.State())
    override val state: Value<FileRenameSettingsComponent.State> = _state

    init {
        loadCourses()
        observeRules()
    }

    override fun onIntent(intent: FileRenameSettingsComponent.Intent) {
        when (intent) {
            is FileRenameSettingsComponent.Intent.AddRule -> addRule(intent.rule)
            is FileRenameSettingsComponent.Intent.DeleteRule -> deleteRule(intent.rule)
            FileRenameSettingsComponent.Intent.Back -> onBack()
            FileRenameSettingsComponent.Intent.Refresh -> loadCourses()
        }
    }

    private fun loadCourses() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val courses = courseRepository.fetchCourses()
                _state.value = _state.value.copy(
                    courses = courses.orEmpty(),
                    isLoading = false,
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to load courses for rename settings" }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить курсы",
                )
            }
        }
    }

    private fun observeRules() {
        renameRepository.rules
            .onEach { rules ->
                _state.value = _state.value.copy(rules = rules)
            }
            .launchIn(scope)
    }

    private fun addRule(rule: FileRenameRule) {
        scope.launch {
            renameRepository.addRule(rule)
        }
    }

    private fun deleteRule(rule: FileRenameRule) {
        scope.launch {
            renameRepository.deleteRule(rule)
        }
    }
}
