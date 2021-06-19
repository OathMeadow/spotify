import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
	java
	application
}

group = "com.spotify"
version = "1.0.0-SNAPSHOT"

repositories {
	mavenCentral()
}

val vertxVersion = "4.1.0"
val junitJupiterVersion = "5.7.2"

val mainVerticleName = "com.spotify.server.MusicRecordVerticle"
val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"
val launcherClassName = "io.vertx.core.Launcher"

application {
	mainClass.set(launcherClassName)
}

dependencies {
	implementation("io.vertx:vertx-web:$vertxVersion")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
	compileOnly("org.projectlombok:lombok:1.18.20")
	annotationProcessor("org.projectlombok:lombok:1.18.20")

	testImplementation("io.vertx:vertx-junit5:$vertxVersion")
	testImplementation("io.vertx:vertx-web-client:$vertxVersion")
	testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
	testImplementation("org.hamcrest:hamcrest-core:2.2")
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaExec> {
	args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

tasks.distTar {
	enabled = false
}

configurations {
	compileOnly {
		extendsFrom(annotationProcessor.get())
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		showStandardStreams = true
		exceptionFormat = FULL
		events = setOf(PASSED, SKIPPED, FAILED)
	}
}
