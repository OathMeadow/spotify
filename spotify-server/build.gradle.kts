
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

val mainVerticleName = "com.spotify.server.ServerVerticle"
val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"
val launcherClassName = "io.vertx.core.Launcher"

configurations {
	compileOnly {
		extendsFrom(annotationProcessor.get())
	}
}

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

tasks.withType<JavaExec> {
	args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}

tasks.distTar {
	enabled = false
}

tasks.jar {
	manifest {
		attributes(
				"Implementation-Title" to "Spotify record server",
				"Implementation-Version" to archiveVersion,
				"Main-Class" to launcherClassName,
				"Main-Verticle" to mainVerticleName
		)
	}
}
