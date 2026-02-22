import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.kotlinCocoapods)
}

object AppInfo {
  const val APP_VERSION: String = "1.0.1"
  const val APP_VERSION_INT: Int = 101
  const val LICENSE_TYPE = "GPL-3.0"
}

kotlin {
  applyDefaultHierarchyTemplate()

  cocoapods {
    version = AppInfo.APP_VERSION
    license = AppInfo.LICENSE_TYPE
    framework {
      baseName = "ComposeApp"
      isStatic = true
      export(libs.decompose)
      export(libs.decompose.lifecycle)

      // Optional, only if you need state preservation on Darwin (Apple) targets
      export(libs.decompose.state.keeper)
    }
    podfile = project.file("../iosApp/podfile")
    xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
    xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
  }

  // Android
  androidLibrary {
    namespace = "com.kmptemplate.library"
    compileSdk = libs.versions.android.max.sdk
      .get()
      .toInt()
    minSdk = libs.versions.android.min.sdk
      .get()
      .toInt()

    // Make sure composeResources/drawable is included in the APK
    androidResources {
      enable = true
    }

    withHostTest {
      isIncludeAndroidResources = true
    }

    withDeviceTest {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      execution = "HOST"
    }
  }

  iosArm64()
  iosSimulatorArm64()

  // Common
  sourceSets {
    commonMain.dependencies {
      api(libs.compose.runtime)
      api(libs.compose.foundation)
      api(libs.compose.material3)
      api(libs.compose.ui)

      api(libs.compose.components.resources)
      api(libs.compose.uiToolingPreview)

      api(libs.androidx.lifecycle.viewmodelCompose)
      api(libs.androidx.lifecycle.runtimeCompose)

      api(libs.decompose)
      implementation(libs.decompose.animations)

      implementation(libs.koin.core)

      implementation(libs.kotlinx.serialization.json)

      implementation(libs.ktor.client.core)
      implementation(libs.ktor.serialization.kotlinx.json)
      implementation(libs.ktor.client.content.negotiation)
    }
    commonTest.dependencies {
      api(libs.kotlin.test)
    }
    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
    }
    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)
      implementation(libs.ktor.client.cio)
    }
  }
}
