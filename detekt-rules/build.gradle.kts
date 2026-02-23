plugins {
    alias(libs.plugins.kotlinJvm)
}
kotlin {
    dependencies {
        implementation(libs.detekt.api)
    }
}
