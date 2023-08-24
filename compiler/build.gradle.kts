plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(projects.annotation)
    implementation(projects.common)

    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.10-1.0.13")

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
