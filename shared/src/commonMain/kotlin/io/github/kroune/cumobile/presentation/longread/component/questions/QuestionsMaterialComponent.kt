package io.github.kroune.cumobile.presentation.longread.component.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.common.RenderComponent
import io.github.kroune.cumobile.presentation.common.ui.AppTheme

/**
 * Simple material component for question-type materials (unsupported on mobile).
 */
class QuestionsMaterialComponent(
    componentContext: ComponentContext,
    private val material: LongreadMaterial,
) : ComponentContext by componentContext,
    RenderComponent {
    @Composable
    override fun Render() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppTheme.colors.surface)
                .padding(16.dp),
        ) {
            Text(
                text = material.contentName ?: "Вопросы",
                color = AppTheme.colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Этот тип материала недоступен в мобильном приложении",
                color = AppTheme.colors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
