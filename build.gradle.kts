import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.allopen) apply false
}

allprojects {
    group = " com.kohan"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "20"
        targetCompatibility = "20"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "20"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    apply(plugin = rootProject.libs.plugins.kotlin.jvm.get().pluginId)
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)

    dependencies {
        implementation(rootProject.libs.kotlin.stdlib)
        implementation(rootProject.libs.kotlin.reflect)
        implementation(rootProject.libs.kotlin.coroutines.core)
        implementation(rootProject.libs.kotlin.coroutines.debug)

        testImplementation(rootProject.libs.kotlin.test)
        testImplementation(rootProject.libs.kotlin.coroutines.test)
    }
}
