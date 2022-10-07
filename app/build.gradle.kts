plugins {
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.android.application)
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.dagger.hilt)
}

android {
    compileSdk = Versions.sdk

    defaultConfig {
        versionCode = 1
        versionName = "1.0"
        applicationId = "jp.takuji31.compose.navigation.example"
        minSdk = 23
        targetSdk = Versions.sdk

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
        kotlinCompilerExtensionVersion = "1.3.2"
    }
}

dependencies {
    implementation(libs.bundles.coroutines)

    implementation(libs.androidx.core)
    implementation(libs.bundles.lifecycle)
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.bundles.compose)
    implementation(libs.activity.compose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.hilt.navigation.compose)

    // For instrumentation tests
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)

    // For local unit tests
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.android.compiler)

    implementation(libs.timber)

    testImplementation(libs.androidx.core.testing)


    implementation(project(":annotation"))
    implementation(project(":library"))
    kapt(project(":compiler"))

    // NOTE: Current limitation this library doesn't work correctly in single-module with kapt.correctErrorTypes=true
    implementation(project(":navigation"))

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.composeVersion}")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.ext:truth:1.4.0")
    androidTestImplementation("com.google.truth:truth:1.1.3")
    androidTestImplementation("androidx.activity:activity-ktx:1.6.0")
}

kapt {
    correctErrorTypes = true
}
