plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects{
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

allprojects {
    repositories {
        mavenCentral()
    }
}
