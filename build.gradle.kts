import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
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
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    val ktlint by configurations.creating

    apply(plugin = rootProject.libs.plugins.kotlin.jvm.get().pluginId)

    dependencies {
        implementation(rootProject.libs.kotlin.stdlib)
        implementation(rootProject.libs.kotlin.reflect)
        implementation(rootProject.libs.kotlin.coroutines.core)
        implementation(rootProject.libs.kotlin.coroutines.debug)
        implementation(rootProject.libs.jackson.module.kotlin)
        implementation(rootProject.libs.dotenv)

        testImplementation(rootProject.libs.kotlin.test)
        testImplementation(rootProject.libs.kotlin.coroutines.test)
        testImplementation(rootProject.libs.spring.boot.starter.test)

        ktlint(of(rootProject.libs.ktlint)) {
            attributes {
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
            }
        }
    }

    val ktlintCheck by tasks.registering(JavaExec::class) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Check Kotlin code style"
        classpath = ktlint
        mainClass.set("com.pinterest.ktlint.Main")
        // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
        args(
            "**/src/**/*.kt",
            "**.kts",
            "!**/build/**",
        )
    }

    tasks.check {
        dependsOn(ktlintCheck)
    }

    tasks.register<JavaExec>("ktlintFormat") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Check Kotlin code style and format"
        classpath = ktlint
        mainClass.set("com.pinterest.ktlint.Main")
        jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
        // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
        args(
            "-F",
            "**/src/**/*.kt",
            "**.kts",
            "!**/build/**",
        )
    }
}

fun of(dependency: Provider<MinimalExternalModuleDependency>): String {
    val lib = dependency.get()
    return "${lib.group}:${lib.name}:${lib.version}"
}
