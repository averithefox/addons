import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("net.fabricmc.fabric-loom-remap")
  `maven-publish`
  id("org.jetbrains.kotlin.jvm") version "2.3.20"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

base {
  archivesName = providers.gradleProperty("archives_base_name")
}

repositories {
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
  // To change the versions see the gradle.properties file
  minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

  // Fabric API. This is technically optional, but you probably want it anyway.
  modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
  modImplementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")

  modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
}

tasks.processResources {
  inputs.property("version", version)

  filesMatching("fabric.mod.json") {
    expand("version" to version)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_21
  }
}

java {
  withSourcesJar()

  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
  inputs.property("archivesName", base.archivesName)

  from("LICENSE") {
    rename { "${it}_${base.archivesName.get()}" }
  }
}
