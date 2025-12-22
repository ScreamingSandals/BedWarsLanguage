package org.screamingsandals.bedwars.lang.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateBuildInfoTask : DefaultTask() {

    @get:InputFiles
    abstract val languageFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val buildNumber: Property<String>

    @TaskAction
    fun generate() {
        val outFile = outputDir.file("language_definition.json").get().asFile
        outFile.parentFile.mkdirs()

        val translationBranch = version.get().split("-", limit = 2)[0]

        outFile.writeText(buildString {
            append("{\"branch\": \"${translationBranch}\", ")
            append("\"version\": \"${buildNumber.get()}\", \"languages\":{")

            languageFiles.files.forEachIndexed { index, file ->
                if (index > 0) {
                    append(",")
                }
                val locale = Regex("[a-z]{2}-[A-Z][A-Za-z]{1,3}").find(file.name)?.value
                append("\"$locale\": \"languages/${file.name}\"")
            }
            append("}}")
        })
    }
}
