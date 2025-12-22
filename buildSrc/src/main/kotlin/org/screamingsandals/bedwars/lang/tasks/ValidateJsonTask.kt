package org.screamingsandals.bedwars.lang.tasks

import com.google.gson.JsonParser
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

abstract class ValidateJsonTask : DefaultTask() {

    @get:InputFiles
    abstract val jsonFiles: ConfigurableFileCollection

    @TaskAction
    fun validate() {
        jsonFiles.files.forEach { jsonFile ->
            try {
                jsonFile.reader().use {
                    JsonParser.parseReader(it)
                }
            } catch (e: Exception) {
                throw GradleException("Invalid JSON: ${jsonFile.path}\n${e.message}")
            }
        }
    }
}
