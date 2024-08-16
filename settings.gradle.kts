plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("dependencies.toml"))
        }
    }
}
include("Authentication-Server", "Push-Server", "Message-REST-Server", "File-Server", "WebSocket-Server")

rootProject.name = "KoHan-Chat-Server"
