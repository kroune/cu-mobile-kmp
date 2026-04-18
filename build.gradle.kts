import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinCocoapods) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.roborazziPlugin) apply false
    alias(libs.plugins.vkompose) apply false
    alias(libs.plugins.androidTest) apply false
    alias(libs.plugins.androidxBaselineprofile) apply false
    idea
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

tasks.register<Exec>("installGitHooks") {
    description = "Configures git to use .githooks directory for hook scripts"
    group = "setup"
    commandLine("git", "config", "core.hooksPath", ".githooks")
}

tasks.named("prepareKotlinBuildScriptModel") {
    dependsOn("installGitHooks")
}

tasks.withType<Wrapper>().configureEach {
    distributionType = Wrapper.DistributionType.ALL
}

allprojects {
    tasks.withType<KotlinCompilationTask<*>>() {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
    afterEvaluate {
        plugins.withId("org.jlleitschuh.gradle.ktlint") {
            extensions.configure<KtlintExtension> {
                reporters {
                    reporter(HTML)
                }
                filter {
                    exclude {
                        it.file.toString().contains("build/generated")
                    }
                }
            }
        }
    }
}
