import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(projects.annotation)
    implementation(projects.common)

    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.21-1.0.11")

    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
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
