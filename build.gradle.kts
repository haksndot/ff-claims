plugins {
    java
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "com.haksnbot"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.mikeprimm.com")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.TechFortress:GriefPrevention:17.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly(files("/home/haksndot/server/plugins/Dynmap.jar"))
    implementation("de.rapha149.signgui:signgui:2.5.4")
}

tasks {
    shadowJar {
        archiveBaseName.set("FFClaims")
        archiveClassifier.set("")
        relocate("de.rapha149.signgui", "com.haksnbot.ffclaims.lib.signgui")
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }
}
