plugins {
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.android.library)
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    compileSdk = Versions.sdk

    defaultConfig {
        minSdk = 23
        targetSdk = Versions.sdk

        namespace = "jp.takuji31.compose.navigation.compile.testing"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }
    }

    sourceSets {
        getByName("main").java.srcDir("src/main/kotlin")
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
}

dependencies {
    implementation(projects.library)

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)

    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(projects.compiler)
}
