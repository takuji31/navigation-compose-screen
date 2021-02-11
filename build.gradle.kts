// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha05")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.hiltVersion}")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}


tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}
