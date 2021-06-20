import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

subprojects {
    apply(plugin="java")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            exceptionFormat = FULL
            events = setOf(PASSED, SKIPPED, FAILED)
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
