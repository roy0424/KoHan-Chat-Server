import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.spring.dependency.management)
    id("com.google.protobuf") version "0.9.2"
}

apply(plugin = "com.google.protobuf")

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
    api(libs.armeria)
    api(libs.armeria.grpc)
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.1")
}

plugins.withType<ProtobufPlugin> {

    // protobuf file 소스 경로 지정
    sourceSets {
        main {
            proto {
                srcDir("/proto/order")
            }
        }
    }

    // protobuf compile, code generate 를 위한 설정
    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:3.25.1"
        }

        plugins {
            id("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:1.64.0"
            }
            id("grpckt") {
                artifact = "io.grpc:protoc-gen-grpc-kotlin:1.64.0:jdk8@jar"
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
