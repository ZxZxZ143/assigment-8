plugins {
    java
    application
    checkstyle
    id("com.github.spotbugs") version "6.4.6"
    id("org.owasp.dependencycheck") version "12.2.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("Main")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "10.12.5"
    configFile = file("config/checkstyle/checkstyle.xml")
}

spotbugs {
    ignoreFailures.set(false)
}

dependencyCheck {
    failBuildOnCVSS = 7.0F
    formats = listOf("HTML", "XML")

    nvd {
        apiKey = System.getenv("NVD_API_KEY") ?: ""
    }

    scanConfigurations = listOf("runtimeClasspath")
}