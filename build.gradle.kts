// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha14")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.hiltVersion}")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}


tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}

subprojects {
    configurations.all {
        resolutionStrategy {
            eachDependency {
                // force kotlinx-collections-immutable-jvm version 0.3.3 to 0.3.4 because 0.3.3 is not available on Maven Central
                if (requested.group == "org.jetbrains.kotlinx" && requested.name == "kotlinx-collections-immutable-jvm" && requested.version == "0.3.3") {
                    useVersion("0.3.4")
                }
            }
        }
    }
}
