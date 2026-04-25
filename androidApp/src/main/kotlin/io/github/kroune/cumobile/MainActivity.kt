package io.github.kroune.cumobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.retainedComponent
import io.github.kroune.cumobile.di.createRootComponent
import io.github.kroune.cumobile.presentation.root.RootComponent

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootComponent = retainedComponent { createRootComponent(it) }

        splashScreen.setKeepOnScreenCondition {
            rootComponent.childStack.value.active.instance is RootComponent.Child.SplashChild
        }

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTagsAsResourceId = true },
            ) {
                App(rootComponent)
            }
        }
    }
}
