tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}

dependencies {
    implementation(projects.modules.domain)
    implementation(projects.modules.application)
    implementation(libs.spring.boot.starter.jpa)
    runtimeOnly(libs.database.h2)
    runtimeOnly(libs.database.mariadb)

    testImplementation(libs.spring.boot.starter.test) {
        exclude(module = "mockito-core")
    }
    testImplementation(libs.database.h2)
}

// JPA Lazy Loading을 위한 final 문제를 해결하기 위해 추가
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

