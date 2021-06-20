#!/bin/bash

echo "Starting Spotify record fetcher"
chmod a+x "${SERVER_HOME}/bin/spotify-server"

params=$@
if [ -n "$params" ]; then
  $SERVER_HOME/bin/spotify-server run -conf ''$params'' com.spotify.server.ServerVerticle
else
  $SERVER_HOME/bin/spotify-server run com.spotify.server.ServerVerticle
fi
