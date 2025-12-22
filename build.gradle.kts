import org.screamingsandals.gradle.builder.configureJavac
import org.screamingsandals.bedwars.lang.tasks.ValidateJsonTask
import org.screamingsandals.bedwars.lang.tasks.GenerateBuildInfoTask
import org.screamingsandals.bedwars.lang.tasks.GenerateLangKeysTask
import org.screamingsandals.gradle.builder.configureSourcesJar
import org.screamingsandals.gradle.builder.setupMavenPublishing
import org.screamingsandals.gradle.builder.setupMavenRepositoriesFromProperties

plugins {
    java
    alias(libs.plugins.screaming.plugin.builder)
}

defaultTasks("clean", "build")

val buildInfoDir = project.layout.buildDirectory.dir("generated/buildinfo")
val buildLangKeysDir = project.layout.buildDirectory.dir("generated/sources/langkeys")
val projectBuildNumber = providers.environmentVariable("BUILD_NUMBER").orElse("custom")

tasks.register<ValidateJsonTask>("validateJson") {
    group = "build"
    description = "Validate all JSON resource files."

    jsonFiles.from(fileTree("src/main/resources/languages") { include("*.json") })
}

tasks.register<GenerateBuildInfoTask>("generateBuildInfoFile") {
    group = "build"

    languageFiles.from(fileTree("src/main/resources/languages") { include("*.json") })
    outputDir.set(buildInfoDir)

    version.set(project.version.toString())
    buildNumber.set(projectBuildNumber)
}

tasks.register<GenerateLangKeysTask>("generateLangKeys") {
    group = "build"
    description = "Generate LangKeys.java from language_en-US.json"

    dependsOn("validateJson")

    packageName.set(providers.gradleProperty("langkeys.package"))
    className.set(providers.gradleProperty("langkeys.class"))

    inputJson.set(layout.projectDirectory.file("src/main/resources/languages/language_en-US.json"))

    outputJava.set(
        buildLangKeysDir
            .map { it.dir(packageName.get().replace(".", "/")) }
            .map { it.file("${className.get()}.java") }
    )
}

sourceSets {
    main {
        java {
            srcDir(buildLangKeysDir)
        }
    }
}

tasks.jar {
    dependsOn("generateBuildInfoFile")
    from(buildInfoDir)
}

configureJavac(JavaVersion.VERSION_11)
setupMavenPublishing {
    pom {
        name.set("BedWars Language")
        description.set("Translation for BedWars Plugin")
        url.set("https://github.com/ScreamingSandals/BedWarsLanguage")
        licenses {
            license {
                name.set("GNU Lesser General Public License v3.0")
                url.set("https://github.com/ScreamingSandals/BedWarsLanguage/blob/0.3.x/LICENSE")
            }
        }

        properties.put("build.number", projectBuildNumber)
    }
}
setupMavenRepositoriesFromProperties()

tasks.compileJava {
    dependsOn("generateLangKeys")
}

tasks.build {
    dependsOn("validateJson")
}