package io.github.kroune.cumobile.presentation.auth.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState

private const val AUTH_URL = "https://my.centraluniversity.ru"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewLoginScreen(component: WebViewLoginComponent) {
    val state by component.state.subscribeAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Авторизация") },
            navigationIcon = {
                TextButton(
                    onClick = { component.onIntent(WebViewLoginComponent.Intent.BackClicked) },
                ) {
                    Text("\u2190 Назад")
                }
            },
        )

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            PlatformWebView(
                url = AUTH_URL,
                onCookieCaptured = { cookie ->
                    component.onIntent(WebViewLoginComponent.Intent.CookieCaptured(cookie))
                },
                modifier = Modifier.fillMaxSize(),
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
