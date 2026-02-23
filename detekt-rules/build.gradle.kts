plugins {
    alias(libs.plugins.kotlinJvm)
}
kotlin {
    dependencies {
        implementation(libs.detekt.api)
        testImplementation(libs.detekt.test)
        testImplementation(libs.detekt.test.utils)
        testImplementation(libs.kotlin.testJunit)
    }
}
