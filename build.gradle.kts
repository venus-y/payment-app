plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
}

// 프로젝트 전역 설정
allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")

    group = "kr.thedream"
    version = "0.0.1-SNAPSHOT"

    java { toolchain { languageVersion = JavaLanguageVersion.of(22) } }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
            // Java 22 툴체인을 사용하되, 바이트코드 타겟은 21로 고정(컴파일러 호환성)
            @Suppress("UnstableApiUsage")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    // Java 컴파일러도 release 21로 정렬하여 Kotlin과 타겟 일치
    tasks.withType<JavaCompile>().configureEach { options.release.set(21) }

    dependencies {
        // kotlin
        implementation("org.jetbrains.kotlin:kotlin-reflect")

        // jackson
        implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

        // test
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("io.mockk:mockk:1.13.5")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testImplementation("com.appmattus.fixture:fixture:1.2.0")
    }
    // Ktlint 설정
    ktlint {
        version.set("0.45.2")
        debug.set(false)
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    extensions.configure<JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }

    tasks.withType<JacocoReport> {
        dependsOn(tasks.withType<Test>())

        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

repositories {
    mavenCentral()
}

// bootstrap
project(":modules:bootstrap") {
    subprojects {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    }
}

// application
project(":modules:application") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}

// domain
project(":modules:domain") {
    // POJO
}

// infra
project(":modules:infrastructure") {
    subprojects {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
        //  코틀린에서 JPA를 사용할 때 발생하는 기본생성자 문제를 해결하기 위해 추가
        apply(plugin = "kotlin-jpa")
    }
}

// common
project(":modules:common") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}

// external
project(":modules:external") {
    subprojects {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    }
}
