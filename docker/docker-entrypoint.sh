#!/bin/bash

echo "Starting Spotify record fetcher"
chmod a+x "${SERVER_HOME}/bin/spotify"
$SERVER_HOME/bin/spotify run com.spotify.ServerVerticle "$@"
