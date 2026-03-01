@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.files.rename

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.local.FileRenameRule
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.DetailTopBar
import io.github.kroune.cumobile.presentation.common.EmptyContent
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
import io.github.kroune.cumobile.presentation.common.stripEmojiPrefix

@Composable
fun FileRenameSettingsScreen(
    component: FileRenameSettingsComponent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        DetailTopBar(
            title = "Шаблоны имен",
            onBack = onBack,
        )

        when {
            state.isLoading && state.courses.isEmpty() -> LoadingContent()
            state.error != null && state.courses.isEmpty() -> ErrorContent(
                error = state.error.orEmpty(),
                onRetry = { component.onIntent(FileRenameSettingsComponent.Intent.Refresh) },
            )
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    AddRuleButton(onClick = { showAddDialog = true })

                    if (state.rules.isEmpty()) {
                        EmptyContent(text = "Нет шаблонов. Добавьте первый!")
                    } else {
                        RulesList(
                            rules = state.rules,
                            onDelete = {
                                component.onIntent(FileRenameSettingsComponent.Intent.DeleteRule(it))
                            },
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRuleDialog(
            courses = state.courses,
            onDismiss = { showAddDialog = false },
            onConfirm = { rule ->
                component.onIntent(FileRenameSettingsComponent.Intent.AddRule(rule))
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun AddRuleButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text = "Добавить шаблон", color = AppColors.Background)
    }
}

@Composable
private fun RulesList(
    rules: List<FileRenameRule>,
    onDelete: (FileRenameRule) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(rules) { rule ->
            RuleCard(rule = rule, onDelete = { onDelete(rule) })
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun RuleCard(
    rule: FileRenameRule,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = rule.extension.uppercase(),
                color = AppColors.Accent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
            TextButton(onClick = onDelete) {
                Text(text = "Удалить", color = AppColors.Error, fontSize = 12.sp)
            }
        }
        Text(
            text = "Для: ${rule.activityName}",
            color = AppColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Шаблон: ${rule.template}",
            color = AppColors.TextSecondary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun AddRuleDialog(
    courses: List<io.github.kroune.cumobile.data.model.Course>,
    onDismiss: () -> Unit,
    onConfirm: (FileRenameRule) -> Unit,
) {
    // Simple inline dialog using Column
    var selectedCourseId by remember { mutableStateOf(courses.firstOrNull()?.id ?: 0) }
    var activityName by remember { mutableStateOf("") }
    var extension by remember { mutableStateOf("pdf") }
    var template by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.Surface)
                .padding(20.dp),
        ) {
            Text(
                text = "Новый шаблон",
                color = AppColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Simplified: manually type everything for now to avoid complex spinners
            Label("Тип работы (например, ДЗ)")
            DarkTextField(
                value = activityName,
                onValueChange = { activityName = it },
                placeholder = "ДЗ",
            )

            Label("Расширение")
            DarkTextField(
                value = extension,
                onValueChange = { extension = it },
                placeholder = "pdf",
            )

            Label("Шаблон имени")
            DarkTextField(
                value = template,
                onValueChange = { template = it },
                placeholder = "HW_{course}_{date}.pdf",
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Отмена", color = AppColors.TextSecondary)
                }
                TextButton(
                    onClick = {
                        if (activityName.isNotBlank() && template.isNotBlank()) {
                            onConfirm(
                                FileRenameRule(
                                    courseId = selectedCourseId,
                                    activityName = activityName,
                                    extension = extension.lowercase(),
                                    template = template,
                                ),
                            )
                        }
                    },
                ) {
                    Text("Добавить", color = AppColors.Accent)
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        color = AppColors.TextSecondary,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp),
    )
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = AppColors.TextSecondary.copy(alpha = 0.5f)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppColors.Background,
            unfocusedContainerColor = AppColors.Background,
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            cursorColor = AppColors.Accent,
            focusedIndicatorColor = AppColors.Accent,
        ),
    )
}
