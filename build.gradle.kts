import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.5"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "net.qilla"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

configurations.create("shade")

dependencies {
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.17.0")
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    withType<ShadowJar> {
        configurations = listOf(project.configurations.getByName("shade"))
        destinationDirectory.set(file("C:\\Users\\Richard\\Development\\Servers\\1.21.3\\plugins"))
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}