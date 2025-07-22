pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.screamingsandals.org/public/")
        // TODO: remove repository when (if) uploaded to gradle plugin portal
        maven("https://maven.neoforged.net/releases") {
            content {
                includeGroup("net.neoforged.licenser")
            }
        }
    }
}


rootProject.name = "BedWarsLanguage"
