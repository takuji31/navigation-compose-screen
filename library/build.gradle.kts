plugins {
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.android.library)
    kotlin("android")
    `maven-publish`
}

android {
    compileSdk = Versions.sdk

    defaultConfig {
        minSdk = 23
        targetSdk = Versions.sdk

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeVersion
    }
}

dependencies {
    api(project(":common"))
    api(project(":annotation"))
    implementation(libs.bundles.coroutines)

    implementation(libs.androidx.core)
    implementation(libs.lifecycle.viewmodel)

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.navigation.compose)

    androidTestImplementation("androidx.compose.ui:ui-test:${Versions.composeVersion}")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.ext:truth:1.4.0")
    androidTestImplementation("com.google.truth:truth:1.1.3")

}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.getByName("release"))

                groupId = Publish.groupId
                artifactId = "navigation-compose-screen"
                version = Publish.version
            }
        }
    }
}
