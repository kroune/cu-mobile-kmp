plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.roborazziPlugin)
    alias(libs.plugins.vkompose)
}

val jdkVersion =
    libs.versions.java
        .get()
        .toInt()

android {
    namespace = "com.thirdparty.cumobile"
    compileSdk =
        libs.versions.android.max.sdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.thirdparty.cumobile"
        minSdk =
            libs.versions.android.min.sdk
                .get()
                .toInt()
        versionCode =
            libs.versions.app.version.code
                .get()
                .toInt()
        versionName =
            libs.versions.app.version.name
                .get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        localeFilters += listOf("en")
    }

    lint {
        abortOnError = false
        checkAllWarnings = true
        warningsAsErrors = false
        checkTestSources = true
        checkDependencies = true
        checkReleaseBuilds = false
        textReport = true
    }

    packaging {
        resources {
            excludes +=
                listOf(
                    "**/*.kotlin_module",
                    "**/*.version",
                    "**/kotlin/**",
                    "**/*.txt",
                    "**/*.xml",
                    "**/*.properties",
                )
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-dev"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        testOptions {
            unitTests {
                isReturnDefaultValues = true
                isIncludeAndroidResources = true
            }
            animationsDisabled = true
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }

        dependenciesInfo {
            includeInApk = false
            includeInBundle = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jdkVersion)
        targetCompatibility = JavaVersion.toVersion(jdkVersion)
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.kotlin.logging)
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiTooling)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.ui.test.junit4)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.composable.preview.scanner)

    debugImplementation(libs.compose.ui.test.manifest)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JavaVersion.toVersion(jdkVersion).toString()))
    }
}

// Shared module compiles to Java 21 bytecode (Gradle daemon JDK),
// so the test worker must also run on Java 21.
tasks.withType<Test>().configureEach {
    javaLauncher =
        javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(21)
        }
}
