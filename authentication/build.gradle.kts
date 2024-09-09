import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.id


plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.protobuf)
}

dependencies {
    implementation(project(":shared-spring"))
    api(libs.armeria.grpc)
}

plugins.withType<ProtobufPlugin> {
    sourceSets {
        main {
            proto {
                srcDir("/proto/order")
            }
        }
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

        generateProtoTasks {
            ofSourceSet("main").forEach {
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
}

fun of(dependency: Provider<MinimalExternalModuleDependency>): String {
    val bom = dependency.get()
    return "${bom.group}:${bom.name}:${bom.version}"
}
