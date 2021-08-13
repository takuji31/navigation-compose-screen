plugins {
    id("java-library")
    id("kotlin")
    kotlin("kapt")
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":annotation"))
    implementation(project(":common"))
    implementation("com.google.auto:auto-common:1.1.2")
    implementation("com.squareup:kotlinpoet:1.9.0")
    @Suppress("AnnotationProcessorOnCompilePath")
    implementation("com.google.auto.service:auto-service:1.0")
    implementation("com.squareup:kotlinpoet-metadata:1.9.0")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.9.0")
    implementation("com.squareup:kotlinpoet-classinspector-elements:1.9.0")
    kapt("com.google.auto.service:auto-service:1.0")
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
