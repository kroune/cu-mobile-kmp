plugins {
    alias(libs.plugins.kotlinJvm)
}
kotlin {
    dependencies {
        compileOnly(libs.detekt.api)
    }
}
