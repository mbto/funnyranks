group = "com.github.mbto.funnyranks"
version = "1.1"

plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

// Dependencies versions without io.spring.dependency-management plugin:
// https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/3.5.7/spring-boot-dependencies-3.5.7.pom
dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
//    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    implementation("com.sun.xml.bind:jaxb-impl:4.0.6")
//    implementation("com.github.ben-manes.caffeine:caffeine")

    implementation("org.jooq:jooq")
    implementation("com.mysql:mysql-connector-j")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")

//    https://repo1.maven.org/maven2/org/joinfaces/joinfaces-platform/5.5.7/joinfaces-platform-5.5.7.pom
//    org.springframework.boot:spring-boot-dependencies:3.5.7
    implementation(platform("org.joinfaces:joinfaces-platform:5.5.7"))
    implementation("org.joinfaces:faces-spring-boot-starter")
    implementation("org.joinfaces:primefaces-spring-boot-starter")
    implementation("org.primefaces.themes:glass-x:1.1.0")
//    implementation("org.joinfaces:omnifaces-spring-boot-starter")
    implementation("org.joinfaces:weld-spring-boot-starter")

    implementation("org.apache.commons:commons-lang3:3.19.0") // 3.17.0 from 3.5.7 (fixes CVE-2025-48924 in org.joinfaces:joinfaces-platform:5.5.7)
    implementation("com.google.code.gson:gson")
    implementation("com.github.jgonian:commons-ip-math:1.32")
    implementation("com.maxmind.geoip2:geoip2:4.4.0")

    testImplementation("org.jooq:jooq-meta")
    testImplementation("org.jooq:jooq-codegen")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    compileJava { options.encoding = "UTF-8" }
    compileTestJava { options.encoding = "UTF-8" }
    bootJar {
        exclude("application.properties")
        exclude("application-dev.properties")
        exclude("application-test.properties")
        archiveFileName = "funnyranks.jar"
    }
    test {
        if(!project.hasProperty("ManualTestEnabled")) {
            exclude("**/ManualTest.class")
        }
        maxParallelForks = Runtime.getRuntime().availableProcessors()
    }
}

val jooqGenDir = layout.buildDirectory.dir("classes/jooqgen")

tasks.register<JavaCompile>("compileModelGenerator") {
    setSource("src/test/java/com/github/mbto/funnyranks/ManualGenerateModel.java")

    classpath = configurations.testRuntimeClasspath.get()
    destinationDirectory.set(jooqGenDir)
    options.encoding = "UTF-8"
}

tasks.register<JavaExec>("generateModel") {
    group = "codegen"
    description = "Generate jOOQ model classes"
    dependsOn("compileModelGenerator")
    classpath = files(
        layout.buildDirectory.dir("classes/jooqgen"),
        configurations.testRuntimeClasspath
    )
    mainClass.set("com.github.mbto.funnyranks.ManualGenerateModel")
    systemProperty("jooq.output.dir", layout.projectDirectory.dir("src/main/java").asFile.absolutePath)
    environment("GENERATE_MODEL", "1")
}