import groovy.json.JsonSlurper
import org.screamingsandals.gradle.builder.configureJavac

plugins {
    java
    alias(libs.plugins.screaming.plugin.builder)
}

defaultTasks("clean", "build")

val buildDir = project.layout.buildDirectory.dir("generated/buildinfo")

sourceSets {
    main {
        output.dir(mapOf("builtBy" to "generateBuildInfoFile"), buildDir)
        resources {
            srcDirs(".")
            include("/languages/**")
        }
    }
}

configureJavac(JavaVersion.VERSION_11)

val jsonValidator = tasks.register("validateJson") {
    group = "verification"
    description = "Validate all JSON resource files."

    doLast {
        fileTree("languages") {
            include("*.json")
        }.forEach { jsonFile ->
            try {
                JsonSlurper().parse(jsonFile)
            } catch (e: Exception) {
                throw GradleException("Invalid JSON: ${jsonFile.relativeTo(projectDir)}\n${e.message}")
            }
        }
    }
}

tasks.named("check") {
    dependsOn(jsonValidator)
}

tasks.register("generateBuildInfoFile") {
    group = "build"

    doFirst {
        mkdir(buildDir)

        buildDir.map { it.file("language_definition.json") }.get().asFile.writeText(buildString {
            val build = if (System.getenv("BUILD_NUMBER") != null) {
                System.getenv("BUILD_NUMBER")
            } else {
                "custom"
            }
            val translationBranch = project.version.toString().split("-", limit=2)[0]

            append("{\"branch\": \"${translationBranch}\", \"version\": \"${build}\", \"languages\":{")

            var first = true
            project.file("languages").listFiles()?.forEach {
                if (!first) {
                    append(",")
                } else {
                    first = false
                }
                val locale = Regex("[a-z]{2}-[A-Z][A-Za-z]{1,3}").find(it.name)?.value
                append("\"${locale}\": \"languages/${it.name}\"")
            }
            append("}}")
        })
    }
}

tasks.named("build") {
    dependsOn("generateBuildInfoFile")
}