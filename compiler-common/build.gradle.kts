import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-library`
    kotlin("kapt")
    `maven-publish`
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(projects.annotation)
    implementation(projects.common)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.metadata)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])

            groupId = Publish.groupId
            artifactId = "compiler"
            version = Publish.version
        }
    }
}
