import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("kapt") version "1.5.0"
    application
}

group = "me.pedro"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/joaophi/*")
        credentials {
            username = project.findProperty("github.actor")?.toString() ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("github.token")?.toString() ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    implementation("com.squareup.okio:okio:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    implementation("com.github.joaophi:yeelight:0.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("me.pedro.yeelight.MainKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "me.pedro.yeelight.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
}