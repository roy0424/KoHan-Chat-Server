plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom(of(libs.boms.micrometer))
        mavenBom(of(libs.boms.netty))
        mavenBom(of(libs.boms.armeria))
        mavenBom(of(libs.boms.resilience4j))
    }
}

dependencies {
    implementation(libs.armeria.spring.boot3.starter)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(libs.kafka)
    implementation(libs.kotlin.allopen)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.debug)
    implementation(libs.bcprov.jdk18on)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    annotationProcessor(libs.spring.boot.configuration.processor)

    runtimeOnly(libs.armeria.spring.boot3.actuator.starter)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

fun of(dependency: Provider<MinimalExternalModuleDependency>): String {
    val bom = dependency.get()
    return "${bom.group}:${bom.name}:${bom.version}"
}
