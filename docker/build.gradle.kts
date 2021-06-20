plugins {
    java
    id("com.palantir.docker") version "0.26.0"
    id("de.undercouch.download") version "4.1.1"
}

val spotify: Configuration by configurations.creating

dependencies {
    spotify(project(":spotify-server"))
}

group = "com.spotify"
version = "1.0.0-SNAPSHOT"

tasks.docker.get().dependsOn("spotifyDistribution")
tasks.register<Copy>("spotifyDistribution") {
    into("$buildDir/spotify")
    from(file(project(":spotify-server").buildDir.absolutePath + "/distributions")) {
        include("*.zip")
    }
    rename(".*.zip", "spotify.zip")
}

docker {
    name = "spotify:$version"
    copySpec.from(tasks.getByName<Copy>("spotifyDistribution").outputs)
    copySpec.from("docker-entrypoint.sh")
}
