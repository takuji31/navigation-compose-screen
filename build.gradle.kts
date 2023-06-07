import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.android.application) apply false
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.android.library) apply false
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.kotlin) apply false
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.dagger.hilt) apply false
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}

/**
 * https://github.com/google/ksp/issues/1288
 */
tasks.withType<KotlinCompile>()
    .configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
