import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.13.1"
    id("org.jetbrains.grammarkit") version "2023.3.0.2"
}

group = "dev.jbosscli"
version = "1.0.1"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.2.6")
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}

kotlin { jvmToolchain(21) }

val generated = layout.buildDirectory.dir("generated/sources/grammar")
sourceSets.main { java.srcDir(generated) }

tasks.generateParser {
    sourceFile.set(file("src/main/grammar/JBossCli.bnf"))
    targetRootOutputDir.set(generated)
    pathToParser.set("dev/jbosscli/parser/JBossCliParser.java")
    pathToPsiRoot.set("dev/jbosscli/psi")
    purgeOldFiles.set(true)
}
tasks.generateLexer {
    sourceFile.set(file("src/main/grammar/JBossCli.flex"))
    targetOutputDir.set(generated.map { it.dir("dev/jbosscli/lexer") })
    purgeOldFiles.set(true)
}
tasks.compileKotlin { dependsOn(tasks.generateParser, tasks.generateLexer) }
tasks.compileJava { dependsOn(tasks.generateParser, tasks.generateLexer) }

intellijPlatform {
    pluginConfiguration {
        name = "JBoss / WildFly CLI"
        ideaVersion { sinceBuild = "252" }
    }
    buildSearchableOptions = false
    pluginVerification {
        ides {
            create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaCommunity, "2025.2.6")
            providers.gradleProperty("verificationIdePath").orNull?.let { local(it) }
        }
    }
}

tasks.test {
    maxHeapSize = "1g"
    systemProperty("java.awt.headless", "true")
}

tasks.wrapper { gradleVersion = "9.4.1" }
