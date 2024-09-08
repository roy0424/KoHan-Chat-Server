import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf


plugins {
    alias(libs.plugins.protobuf)
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
    protobuf(project(":proto"))

    api(libs.kafka)

    api(libs.armeria.kotlin)
    api(libs.armeria.grpc)

    api(libs.grpc.stub)
    api(libs.grpc.protobuf)
    api(libs.grpc.kotlin.stub)

    api(libs.protobuf.kotlin)

    implementation(libs.uap.java)
}

protobuf {
    protoc {
        artifact = of(libs.artifact.protoc)
    }

    plugins {
        id("grpc") {
            artifact = of(libs.artifact.gen.grpc.java)
        }
        id("grpckt") {
            artifact = of(libs.artifact.gen.grpc.kotlin) + ":jdk8@jar"
        }
    }

    // generate code
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

fun of(dependency: Provider<MinimalExternalModuleDependency>): String {
    val bom = dependency.get()
    return "${bom.group}:${bom.name}:${bom.version}"
}
