plugins {
    alias(libs.plugins.spring.dependency.management)
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
    api(libs.kafka)
}

fun of(dependency: Provider<MinimalExternalModuleDependency>): String {
    val bom = dependency.get()
    return "${bom.group}:${bom.name}:${bom.version}"
}
