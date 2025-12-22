package org.screamingsandals.bedwars.lang.tasks

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GenerateLangKeysTask : DefaultTask() {

    @get:InputFile
    abstract val inputJson: RegularFileProperty

    @get:OutputFile
    abstract val outputJava: RegularFileProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val className: Property<String>

    @TaskAction
    fun generate() {
        val root = inputJson.get().asFile.reader().use {
            JsonParser.parseReader(it)
        }

        val entries = mutableListOf<List<String>>()

        fun walk(node: JsonElement, path: List<String>) {
            if (node.isJsonObject) {
                node.asJsonObject.entrySet().forEach { (key, value) ->
                    walk(value, path + key)
                }
            } else {
                entries += path
            }
        }

        walk(root, emptyList())

        val javaFile = outputJava.get().asFile
        javaFile.parentFile.mkdirs()

        javaFile.writeText(buildString {
            appendLine("package ${packageName.get()};")
            appendLine("public final class ${className.get()} {")
            appendLine("private ${className.get()}() {}")

            entries.forEach { path ->
                val fieldName = path.joinToString("_") { it.uppercase() }
                val fieldValue = path.joinToString(", ") { "\"$it\"" }
                appendLine("public static final String[] $fieldName = {$fieldValue};")
            }

            appendLine("}")
        })
    }
}