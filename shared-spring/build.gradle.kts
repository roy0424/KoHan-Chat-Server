plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.allopen)
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    api(project(":shared-armeria")) {
        exclude(
            group =
                libs.slf4j.simple
                    .get()
                    .group,
            module =
                libs.slf4j.simple
                    .get()
                    .name,
        )
        exclude(
            module = project(":proto").name,
        )
    }
    api(project(":shared-collection"))
    api(libs.kotlin.allopen)
    api(libs.armeria.spring.boot3.starter)
    api(libs.spring.boot.starter.security)
    api(libs.spring.boot.starter.validation)
    api(libs.spring.boot.starter.data.mongodb)
    api(libs.spring.kafka)
    api(libs.bcprov.jdk18on)
    api(libs.spring.embed.mongo)

    api(libs.uap.java)

    testApi(libs.embed.mongo)
    annotationProcessor(libs.spring.boot.configuration.processor)

    runtimeOnly(libs.armeria.spring.boot3.actuator.starter)
}

tasks.bootJar { enabled = false }

tasks.jar { enabled = true }
