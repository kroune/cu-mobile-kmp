package io.github.kroune.cumobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.retainedComponent
import io.github.kroune.cumobile.di.createRootComponent
import io.github.kroune.cumobile.presentation.root.RootComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootComponent = retainedComponent { createRootComponent(it) }

        splashScreen.setKeepOnScreenCondition {
            rootComponent.childStack.value.active.instance is RootComponent.Child.SplashChild
        }

        setContent {
            App(rootComponent)
        }
    }
}
