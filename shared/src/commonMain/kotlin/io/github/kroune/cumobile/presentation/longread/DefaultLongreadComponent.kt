package io.github.kroune.cumobile.presentation.longread

import androidx.compose.material3.SnackbarResult
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.items.Items
import com.arkivanov.decompose.router.items.ItemsNavigation
import com.arkivanov.decompose.router.items.LazyChildItems
import com.arkivanov.decompose.router.items.childItems
import com.arkivanov.decompose.router.items.setItems
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.longread.LongreadComponent.Intent
import io.github.kroune.cumobile.presentation.longread.component.ExternalUpdate
import io.github.kroune.cumobile.presentation.longread.component.LongreadItem
import io.github.kroune.cumobile.presentation.longread.component.MaterialConfig
import io.github.kroune.cumobile.presentation.longread.component.audio.AudioMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.coding.DefaultCodingMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.file.FileDownloadResult
import io.github.kroune.cumobile.presentation.longread.component.file.FileMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.image.ImageMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.markdown.MarkdownMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.questions.QuestionsMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.video.VideoMaterialComponent
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Default implementation of [LongreadComponent].
 *
 * Uses Decompose's ChildItems to give each material its own component
 * with automatic lifecycle management via [ChildItemsLifecycleController].
 */
@OptIn(ExperimentalDecomposeApi::class)
class DefaultLongreadComponent(
    componentContext: ComponentContext,
    params: LongreadParams,
    deps: LongreadDependencies,
    private val onBack: () -> Unit,
    private val onDownloadReady: suspend (url: String, filename: String) -> Boolean,
    private val onNavigateToFiles: () -> Unit,
) : LongreadComponent,
    ComponentContext by componentContext {
    private val contentRepository = deps.contentRepository
    private val taskRepository = deps.taskRepository
    private val renameRepository = deps.renameRepository

    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(
        LongreadComponent.State(
            longreadId = params.longreadId,
            courseId = params.courseId,
            themeId = params.themeId,
        ),
    )
    override val state: Value<LongreadComponent.State> = _state

    private val _effects = Channel<LongreadComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<LongreadComponent.Effect> = _effects.receiveAsFlow()

    private val searchHandler = LongreadSearchHandler(state = _state)

    /** Broadcasts [ExternalUpdate] events to material child components. */
    private val _externalUpdates = MutableSharedFlow<ExternalUpdate>(
        replay = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val externalUpdates: Flow<ExternalUpdate> = _externalUpdates

    /** Lookup map from material ID to material data, used by the child factory. */
    private var materialsMap: Map<String, LongreadMaterial> = emptyMap()

    private val navigation = ItemsNavigation<MaterialConfig>()

    override val materialItems: LazyChildItems<MaterialConfig, LongreadItem> = childItems(
        source = navigation,
        serializer = MaterialConfig.serializer(),
        initialItems = { Items(items = emptyList()) },
        key = "LongreadMaterialItems",
        childFactory = ::createChild,
    )

    override fun onIntent(intent: Intent) {
        when (intent) {
            is Intent.Navigation -> handleNavigationIntent(intent)
            is Intent.Search -> handleSearchIntent(intent)
        }
    }

    private fun handleNavigationIntent(intent: Intent.Navigation) {
        when (intent) {
            Intent.Navigation.Back -> onBack()
            Intent.Navigation.Refresh -> loadMaterials()
            Intent.Navigation.NavigateToFiles -> onNavigateToFiles()
        }
    }

    private fun handleSearchIntent(intent: Intent.Search) {
        when (intent) {
            Intent.Search.ToggleSearch -> {
                searchHandler.toggleSearch()
                if (!_state.value.isSearchVisible) {
                    _externalUpdates.tryEmit(ExternalUpdate.SearchQuery(""))
                }
            }
            Intent.Search.NextMatch -> searchHandler.navigateMatch(forward = true)
            Intent.Search.PreviousMatch -> searchHandler.navigateMatch(forward = false)
            is Intent.Search.UpdateSearchQuery -> {
                searchHandler.updateSearchQuery(intent.query)
                _externalUpdates.tryEmit(ExternalUpdate.SearchQuery(intent.query))
            }
        }
    }

    init {
        loadMaterials()
    }

    private fun loadMaterials() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            navigation.setItems { emptyList() }

            val materials = contentRepository.fetchLongreadMaterials(
                _state.value.longreadId,
            )
            if (materials != null) {
                _state.value = _state.value.copy(
                    materials = materials.toPersistentList(),
                    isLoading = false,
                )
                materialsMap = materials.associateBy { it.id }
                val configs = materials.map { it.toConfig() }
                navigation.setItems { configs }
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить материалы",
                )
            }
        }
    }

    private fun createChild(
        config: MaterialConfig,
        childContext: ComponentContext,
    ): LongreadItem {
        val material = materialsMap[config.id] ?: LongreadMaterial(id = config.id)
        return when (config) {
            is MaterialConfig.Markdown -> LongreadItem.Markdown(
                MarkdownMaterialComponent(
                    componentContext = childContext,
                    material = material,
                    externalUpdates = externalUpdates,
                ),
            )

            is MaterialConfig.File -> LongreadItem.File(
                createFileComponent(childContext, material),
            )

            is MaterialConfig.Coding -> LongreadItem.Coding(
                DefaultCodingMaterialComponent(
                    componentContext = childContext,
                    material = material,
                    taskId = config.taskId,
                    taskRepository = taskRepository,
                    contentRepository = contentRepository,
                    onShowError = { msg ->
                        _effects.trySend(LongreadComponent.Effect.SnackBarEffect(msg))
                    },
                    onSaveFile = onDownloadReady,
                ),
            )

            is MaterialConfig.Questions -> LongreadItem.Questions(
                QuestionsMaterialComponent(
                    componentContext = childContext,
                    material = material,
                ),
            )

            is MaterialConfig.Image -> LongreadItem.Image(
                ImageMaterialComponent(
                    componentContext = childContext,
                    material = material,
                ),
            )

            is MaterialConfig.Video -> LongreadItem.Video(
                VideoMaterialComponent(
                    componentContext = childContext,
                    material = material,
                ),
            )

            is MaterialConfig.Audio -> LongreadItem.Audio(
                AudioMaterialComponent(
                    componentContext = childContext,
                    material = material,
                ),
            )
        }
    }

    private fun createFileComponent(
        childContext: ComponentContext,
        material: LongreadMaterial,
    ) =
        FileMaterialComponent(
            componentContext = childContext,
            material = material,
            contentRepository = contentRepository,
            resolveFilename = { buildLocalFilename(material) },
            saveFile = onDownloadReady,
            onDownloadResult = ::handleDownloadResult,
        )

    private fun handleDownloadResult(result: FileDownloadResult) {
        val effect = when (result) {
            FileDownloadResult.Started -> LongreadComponent.Effect.SnackBarEffect(
                message = "Скачивание...",
            )
            FileDownloadResult.Success -> LongreadComponent.Effect.SnackBarEffect(
                message = "Файл сохранён в «Файлы»",
                actionLabel = "Перейти к файлу",
                onSnackbarResult = { snackbarResult ->
                    if (snackbarResult == SnackbarResult.ActionPerformed) {
                        onNavigateToFiles()
                    }
                },
            )
            is FileDownloadResult.Failed -> LongreadComponent.Effect.SnackBarEffect(
                message = result.message,
            )
        }
        _effects.trySend(effect)
    }

    private suspend fun buildLocalFilename(material: LongreadMaterial): String {
        val filename = material.filename ?: "file"
        val version = material.version ?: "1"
        val dotIndex = filename.lastIndexOf('.')
        val extension = if (dotIndex > 0) filename.substring(dotIndex + 1) else ""

        val activityName = material.estimation?.activityName
        if (activityName != null) {
            val rule = renameRepository.getMatchingRule(
                courseId = _state.value.courseId,
                activityName = activityName,
                extension = extension,
            )
            if (rule != null) {
                return rule.apply(
                    courseName = _state.value.title,
                    activityName = activityName,
                    version = version,
                )
            }
        }

        val baseName = if (dotIndex > 0) filename.substring(0, dotIndex) else filename
        val safeName = baseName.replace(UNSAFE_CHARS_REGEX, "_")
        val extPart = if (extension.isNotEmpty()) ".$extension" else ""
        return "${safeName}_$version$extPart"
    }

    companion object {
        private val UNSAFE_CHARS_REGEX = Regex("[^a-zA-Z0-9._-]")
    }
}

/**
 * Maps a [LongreadMaterial] to its corresponding [MaterialConfig].
 */
private fun LongreadMaterial.toConfig(): MaterialConfig =
    when {
        isCoding && taskId != null -> MaterialConfig.Coding(id, taskId = taskId)
        isFile -> MaterialConfig.File(id)
        isQuestions -> MaterialConfig.Questions(id)
        isImage -> MaterialConfig.Image(id)
        isVideo || isVideoPlatform -> MaterialConfig.Video(id)
        isAudio -> MaterialConfig.Audio(id)
        else -> MaterialConfig.Markdown(id)
    }
