plugins {
    alias(libs.plugins.androidTest)
    alias(libs.plugins.androidxBaselineprofile)
    alias(libs.plugins.ktlint)
}

val jdkVersion =
    libs.versions.java
        .get()
        .toInt()

android {
    namespace = "com.thirdparty.cumobile.baselineprofile"
    compileSdk =
        libs.versions.android.max.sdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.android.min.sdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.max.sdk
                .get()
                .toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jdkVersion)
        targetCompatibility = JavaVersion.toVersion(jdkVersion)
    }

    targetProjectPath = ":androidApp"

    testOptions {
        managedDevices {
            localDevices {
                // API 31 is the earliest level with full AOT install-time compilation,
                // so the generated profile reflects what production users on 31+ get.
                // Uses the "default" (AOSP) system image — no Play services, headless-friendly.
                create("pixel6Api31") {
                    device = "Pixel 6"
                    apiLevel = 31
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

baselineProfile {
    useConnectedDevices = false
    managedDevices += "pixel6Api31"
}

dependencies {
    implementation(projects.baselineProfileTags)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.testExt.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.uiautomator)
    implementation(libs.junit)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JavaVersion.toVersion(jdkVersion).toString()))
    }
}
