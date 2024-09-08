import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.protobuf)
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
    api(libs.armeria.kotlin)
    api(libs.armeria.grpc)
    api(libs.grpc.kotlin.stub)
    api(libs.protobuf.kotlin)
    api(libs.protobuf.jackson)

    implementation(libs.uap.java)
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
