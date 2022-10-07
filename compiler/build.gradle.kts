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
    implementation(project(":annotation"))
    implementation(project(":common"))
    implementation("com.google.auto:auto-common:1.2.1")
    implementation("com.squareup:kotlinpoet:1.12.0")
    @Suppress("AnnotationProcessorOnCompilePath")
    implementation("com.google.auto.service:auto-service:1.0.1")
    implementation("com.squareup:kotlinpoet-metadata:1.10.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
    compileOnly("net.ltgt.gradle.incap:incap:0.3")
    kapt("net.ltgt.gradle.incap:incap-processor:0.3")
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
