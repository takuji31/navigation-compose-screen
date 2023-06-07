plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])

            groupId = Publish.groupId
            artifactId = "common"
            version = Publish.version
        }
    }
}
