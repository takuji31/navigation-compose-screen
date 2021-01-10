plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        versionCode(1)
        versionName("1.0")
        applicationId("jp.takuji31.compose.navigation.example")
        minSdkVersion(23)
        targetSdkVersion(30)

        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }

    composeOptions {
        kotlinCompilerVersion = Versions.kotlinVersion
        kotlinCompilerExtensionVersion = Versions.composeVersion
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycleVersion}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycleVersion}")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.2.1")

    implementation("androidx.compose.ui:ui:${Versions.composeVersion}")
    implementation("androidx.compose.ui:ui-tooling:${Versions.composeVersion}")
    implementation("androidx.compose.foundation:foundation:${Versions.composeVersion}")
    implementation("androidx.compose.material:material:${Versions.composeVersion}")
    implementation("androidx.compose.material:material-icons-extended:${Versions.composeVersion}")
    implementation("androidx.navigation:navigation-compose:1.0.0-alpha04")

    androidTestImplementation("androidx.compose.ui:ui-test:${Versions.composeVersion}")

    implementation("com.google.dagger:hilt-android:${Versions.hiltVersion}")
    kapt("com.google.dagger:hilt-android-compiler:${Versions.hiltVersion}")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha02")
    kapt("androidx.hilt:hilt-compiler:1.0.0-alpha02")

    // For instrumentation tests
    androidTestImplementation("com.google.dagger:hilt-android-testing:${Versions.hiltVersion}")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:${Versions.hiltVersion}")

    // For local unit tests
    testImplementation("com.google.dagger:hilt-android-testing:${Versions.hiltVersion}")
    kaptTest("com.google.dagger:hilt-android-compiler:${Versions.hiltVersion}")

    implementation("io.coil-kt:coil:1.1.0")
    implementation("dev.chrisbanes.accompanist:accompanist-coil:0.4.1")

    implementation("com.jakewharton.timber:timber:4.7.1")

    testImplementation("androidx.arch.core:core-testing:2.1.0")


    implementation(project(":annotation"))
    implementation(project(":library"))
    kapt(project(":compiler"))

}

kapt {
    correctErrorTypes = true
}
