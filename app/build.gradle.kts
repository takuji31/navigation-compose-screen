plugins {
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.android.application)
    kotlin("android")
    id("kotlin-parcelize")
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "jp.takuji31.compose.navigation.example"
    compileSdk = Versions.compileSdk

    defaultConfig {
        versionCode = 1
        versionName = "1.0"
        applicationId = "jp.takuji31.compose.navigation.example"
        minSdk = 23
        targetSdk = Versions.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }
    }

    sourceSets {
        getByName("main").java.srcDir("src/main/kotlin")
        getByName("debug").java.srcDir("src/main/kotlin")
        getByName("test").java.srcDir("src/test/kotlin")
        getByName("androidTest").java.srcDir("src/androidTest/kotlin")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packagingOptions {
        resources {
            excludes.addAll(
                listOf(
                    "META-INF/AL2.0",
                    "META-INF/LGPL2.1"
                )
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    implementation(libs.bundles.coroutines)

    implementation(libs.androidx.core)
    implementation(libs.bundles.lifecycle)
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.hilt.navigation.compose)

    // For instrumentation tests
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)

    // For local unit tests
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.android.compiler)

    implementation(libs.timber)

    testImplementation(libs.androidx.core.testing)


    implementation(projects.annotation)
    implementation(projects.library)
    testImplementation(projects.compiler)
    ksp(projects.compiler)


    // NOTE: Current limitation this library doesn't work correctly in single-module with kapt.correctErrorTypes=true
    implementation(project(":navigation"))

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.ext:truth:1.5.0")
    androidTestImplementation("com.google.truth:truth:1.4.2")
    androidTestImplementation("androidx.activity:activity-ktx:1.8.2")

    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
