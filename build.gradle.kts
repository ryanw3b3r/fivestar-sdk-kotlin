plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("maven-publish")
}

group = "com.fivestar.support"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")

    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Ktor client for HTTP requests
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // kotlinx.serialization for JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("FiveStar Support Android SDK")
                description.set("Android SDK for FiveStar Support feedback collection")
                url.set("https://fivestar.support")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("ryanweber")
                        name.set("Ryan Weber")
                        email.set("tech@ryan-weber.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/ryanw3b3r/fivestar-sdk-kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com/ryanw3b3r/fivestar-sdk-kotlin.git")
                    url.set("https://github.com/ryanw3b3r/fivestar-sdk-kotlin")
                }
            }
        }
    }
}
