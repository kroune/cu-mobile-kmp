plugins {
    alias(libs.plugins.kotlinJvm)
}
kotlin {
    dependencies {
        implementation(libs.detekt.api)
        implementation(libs.kotlin.logging)
        testImplementation(libs.detekt.test)
        testImplementation(libs.detekt.test.utils)
        testImplementation(libs.kotlin.testJunit)
    }
}
