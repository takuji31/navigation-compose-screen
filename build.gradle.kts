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
