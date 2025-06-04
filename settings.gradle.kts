pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Tambahkan repository untuk plugin compose jika diperlukan
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // Tambahkan repository untuk compose jika diperlukan
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

rootProject.name = "DriveSafer"
include(":app")