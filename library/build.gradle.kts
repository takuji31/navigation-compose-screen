plugins {
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.android.library)
    kotlin("android")
    `maven-publish`
}

android {
    namespace = "jp.takuji31.compose.navigation"
    compileSdk = Versions.compileSdk

    defaultConfig {
        minSdk = 23
        targetSdk = Versions.targetSdk

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
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    api(projects.common)
    api(projects.annotation)
    implementation(libs.bundles.coroutines)

    implementation(libs.androidx.core)
    implementation(libs.lifecycle.viewmodel)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    api(libs.navigation.compose)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.ext:truth:1.5.0")
    androidTestImplementation("com.google.truth:truth:1.4.2")

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
