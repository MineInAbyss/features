rootProject.name = "dependencies"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }
}

val conventionsVersion: String by settings

dependencyResolutionManagement {
    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }
    versionCatalogs {
        create("miaConventions") {
            from("com.mineinabyss.conventions:catalog:$conventionsVersion")
        }
    }
}

include("core")