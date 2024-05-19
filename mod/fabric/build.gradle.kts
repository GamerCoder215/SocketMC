@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("com.modrinth.minotaur") version "2.+"
}

description = "Fabric Mod for SocketMC Client-side Implementation"

val minecraft = project.ext["minecraft_version"].toString()
val parchment = project.ext["parchment"].toString()

val fabric = "0.98.0"

dependencies {
    api(project(":socketmc-core"))

    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraft:$parchment@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:0.15.1")

    setOf(
        "fabric-api-base",
        "fabric-networking-api-v1",
        "fabric-rendering-v1"
    ).forEach {
        modImplementation(fabricApi.module(it, "$fabric+$minecraft"))
    }

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

loom {
    accessWidenerPath = file("src/main/resources/socketmc.accesswidener")
}

tasks {
    jar {
        from(project(":socketmc-core").sourceSets["main"].output)
    }

    processResources {
        filesMatching("**/*.json") {
            expand(project.properties)
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set(project.ext["id"].toString())

    versionName.set("SocketMC v$version")
    versionNumber.set(version.toString())
    versionType.set(project.ext["version_type"].toString())

    uploadFile.set(tasks.jar)
    gameVersions.add(project.ext["minecraft_version"].toString())
    changelog.set(project.ext["changelog"].toString())

    loaders.addAll(listOf("fabric", "quilt"))
    dependencies {
        required.project("fabric-api")
    }

    syncBodyFrom.set(rootProject.file("README.md").bufferedReader().use { it.readText() })
}