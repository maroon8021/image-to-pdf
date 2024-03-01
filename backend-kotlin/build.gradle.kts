
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.8.22"
    id("io.ktor.plugin") version "2.3.8"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-host-common")
    implementation("org.apache.pdfbox:pdfbox:2.0.28")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.drewnoakes:metadata-extractor:2.15.0")
    implementation("org.apache.commons:commons-imaging:1.0-alpha2")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")


}
kotlin {
    jvmToolchain(8)
}