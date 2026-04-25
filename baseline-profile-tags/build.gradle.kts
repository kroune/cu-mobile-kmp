plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.ktlint)
}

kotlin {
    applyDefaultHierarchyTemplate()

    android {
        namespace = "com.thirdparty.cumobile.baseline.tags"
        compileSdk = libs.versions.android.max.sdk
            .get()
            .toInt()
        minSdk = libs.versions.android.min.sdk
            .get()
            .toInt()
    }

    iosArm64()
    iosSimulatorArm64()
}
