package io.github.kroune.cumobile.presentation.longread.ui.questions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val FRACTION_GREEN = 0.4f
private const val FRACTION_YELLOW = 0.1f
private const val BLINK_DURATION_MS = 600

@Composable
fun QuizTimerBar(
    remainingSeconds: Long,
    totalSeconds: Long,
) {
    if (totalSeconds <= 0) return
    val fraction = (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)

    val timerColor by animateColorAsState(
        targetValue = when {
            fraction > FRACTION_GREEN -> Color(0xFF4CAF50)
            fraction > FRACTION_YELLOW -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        },
        label = "timerColor",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "timerBlink")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(BLINK_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blinkAlpha",
    )
    val blinkAlpha = if (fraction <= FRACTION_YELLOW) animatedAlpha else 1f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.weight(1f).height(8.dp),
                color = timerColor.copy(alpha = blinkAlpha),
                trackColor = timerColor.copy(alpha = 0.2f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatTime(remainingSeconds),
                color = timerColor.copy(alpha = blinkAlpha),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private const val SecondsPerMinute = 60
private const val SecondsPerHour = 3600

private fun formatTime(seconds: Long): String {
    val h = seconds / SecondsPerHour
    val m = (seconds % SecondsPerHour) / SecondsPerMinute
    val s = seconds % SecondsPerMinute
    return if (h > 0) {
        "$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    } else {
        "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }
}
