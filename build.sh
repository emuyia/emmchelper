#!/bin/bash

# 1. Stop server via RCON
echo "Sending stop command via RCON..."
#mcrcon -H localhost -P 25576 "plugman reload emMCHelper"
mcrcon -H localhost -P 25576 -p "123" "stop"
if [ $? -ne 0 ]; then
  echo "RCON command failed!" # Added error message
  # exit 1 # Decide if this should be a fatal error
fi
echo "Server stop command sent."

echo "Starting build process..."

# 2. Maven Build
mvn clean package
if [ $? -ne 0 ]; then
  echo "Maven build failed!" # Added error message
  exit 1
fi
echo "Maven build successful."

# 2. Copy JAR
# Find the primary JAR. This assumes only one emMCHelper-*.jar is produced as the main artifact.
# This JAR will be the one that contains the shaded dependencies if the shade plugin
# is configured to replace the main artifact.
PRIMARY_JAR=$(find target -maxdepth 1 -name "emMCHelper-*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" -print -quit)


if [ -z "$PRIMARY_JAR" ]; then
  echo "Error: Primary JAR (emMCHelper-*.jar) not found in target directory." # Updated error message
  exit 1
fi

echo "Found primary JAR: $PRIMARY_JAR"
DEST_JAR="server/plugins/emMCHelper.jar" # The destination name is without version

cp "$PRIMARY_JAR" "$DEST_JAR"
if [ $? -ne 0 ]; then
  echo "Copying JAR to $DEST_JAR failed!" # Added error message
  exit 1
fi
echo "JAR copied to $DEST_JAR successfully."

echo "Build and deploy process complete."

cd server
PAPER_JAR="paper.jar"

java -Xmx4G -Xms4G -jar "$PAPER_JAR" --nogui
if [ $? -ne 0 ]; then
  echo "Failed to start the server with $PAPER_JAR!" # Added error message
  exit 1
fi
echo "Server started successfully with $PAPER_JAR."

exit 0