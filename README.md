[![Build Status](https://www.travis-ci.com/OathMeadow/spotify.svg?branch=main)](https://www.travis-ci.com/OathMeadow/spotify)

# Spotify Record Server
A small Vert.x server that can be used
to retrieve the most popular music
records of 2019.

## Building the server

Clone the git repository

```
> git clone git@github.com:OathMeadow/spotify.git
```

Initialize Gradle

```
> ./gradlew init
```

Build the server

```
> ./gradlew build
```

The server distribution zip can be found in:
```
spotify-server/build/distributions
```

## Running the server

After you have built the server, you may
unzip the distribution to any arbitrary location.
You can run the server from the shell script in the
bin folder using the vert.x start commands.

Below will start the server on default port 8080.

```
> ./spotify-server run com.spotify.server.ServerVerticle
```

You may configure the server port by specifying the following
configuration.

```
> ./spotify-server run -conf '{"PORT":8081}' com.spotify.server.ServerVerticle
```

## Building the docker container

You may build the docker container simply through gradle.
This of course requires the docker service to be installed
on you computer.

```
> ./gradlew build docker
```

and to run the container

```
> docker run --name spotify -d -p 8080:8080 spotify:1.0.0-SNAPSHOT '{"port":8080}'
```

## REST API
You can look at the swagger.yaml to see the
REST API documentation. See below for some
curl examples:


List all records
```
> curl -v http://localhost:8080/api/records
```

Fetch a particular record by rank id
```
> curl -v http://localhost:8080/api/records/15
```

List all records with a certain popularity
```
> curl -v http://localhost:8080/api/records?var=popularity&value=89
```

Below is a list of valid var values:

* rank
* artistName
* trackName
* genre
* length
* popularity
