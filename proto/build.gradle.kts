import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf

plugins {
    alias(libs.plugins.protobuf)
}

dependencies {
    api(libs.grpc.stub)
    api(libs.grpc.protobuf)
    api(libs.grpc.kotlin.stub)

    api(libs.protobuf.kotlin)
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
    val lib = dependency.get()
    return "${lib.group}:${lib.name}:${lib.version}"
}
