plugins {
    java
}

group = "tlm.dev.basalt.demo"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenLocal {
        content {
            includeGroup("io.github.basalt-lib")
        }
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

val paperApi = "io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT"
val basaltApi = "io.github.basalt-lib:basalt-api:0.1.0-SNAPSHOT"

dependencies {
    compileOnly(paperApi)
    compileOnly(basaltApi)
    testImplementation(paperApi)
    testImplementation(basaltApi)
    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.processResources {
    val version = project.version.toString()
    inputs.property("version", version)
    filesMatching("paper-plugin.yml") {
        expand("version" to version)
    }
}

tasks.test {
    useJUnitPlatform()
    systemProperty("demo.root", layout.projectDirectory.asFile.absolutePath)
    inputs.files(fileTree("src"), "build.gradle.kts", "settings.gradle.kts")
        .withPropertyName("seamSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
