plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "ib.translator"

// Функции для работы с версией
val versionFile = File("version.txt")

fun getVersion(): String {
    if (!versionFile.exists()) {
        versionFile.writeText("0.1.0")
    }
    return versionFile.readText().trim()
}

fun incrementVersion() {
    val versionParts = versionFile.readText().trim().split(".")
    val newPatchVersion = versionParts.last().toInt() + 1
    val newVersion = versionParts.dropLast(1) + newPatchVersion.toString()
    versionFile.writeText(newVersion.joinToString("."))
}

version = getVersion()
// Настройка задачи test
tasks.test {
    useJUnitPlatform()
}
// Настройка задачи shadowJar
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    dependsOn("test") // Задача shadowJar зависит от задачи test
    finalizedBy("incrementVersion")
    finalizedBy("runCmdScript")
}
tasks.register("incrementVersion") {
    doLast {
        incrementVersion()
    }
}
val jarFileTask = tasks.register("jarFile") {
    dependsOn("shadowJar")
    doLast {
        val shadowJarTask = tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").get()
        val jarFile = shadowJarTask.archiveFile.get().asFile
        println("File name: ${jarFile.name}")
        project.extra["jarFile"] = jarFile
        project.extra["jarFileName"] = jarFile.name
    }
}
tasks.register("runCmdScript") {
    dependsOn("jarFile")
    doLast {
        val jarFile = project.extra["jarFile"] as File
        println("File name: ${jarFile.name}")
//        exec {
//            commandLine("cmd", "/c", "D:/IntelliJProject/ModuleCI/ModuleCI/build/libs/CI-SCRYPT.bat ${project.projectDir}/build/libs/${jarFile.name} ${project.projectDir}")
//        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(files("D:/IntelliJProject/ibAssembly/build/classes/kotlin/main"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    //R2SQL
    implementation("io.r2dbc:r2dbc-spi:0.8.6.RELEASE")
    implementation("org.mariadb:r2dbc-mariadb:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.2")
    // Для работы с конфигурацией
    implementation("com.typesafe:config:1.4.2")
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.12")
    // Kafka
    implementation("org.apache.kafka:kafka-clients:3.9.0")
}

application { mainClass.set("ib.translator.MainKt") }

kotlin {
    jvmToolchain(17)
}
