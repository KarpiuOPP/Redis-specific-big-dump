plugins {
    java
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    compileOnly("org.projectlombok:lombok:1.18.26")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.5")
    annotationProcessor("org.projectlombok:lombok")
    implementation("redis.clients:jedis")
    implementation("org.apache.commons:commons-csv:1.10.0")

}

tasks.withType<Test> {
    useJUnitPlatform()
}
