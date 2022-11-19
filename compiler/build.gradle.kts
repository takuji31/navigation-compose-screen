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

    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.20-1.0.8")

    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
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
