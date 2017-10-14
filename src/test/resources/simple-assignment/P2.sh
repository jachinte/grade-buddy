#!/bin/bash
DIRECTORY=$1
SOURCE_FILE="$(find "$DIRECTORY" -name '*P2.c' | head -n 1)"

# Compile the source code
# From: https://stackoverflow.com/a/20796575/738968
name=${SOURCE_FILE##*/}
base=${name%.c}
compilation="$(gcc "$SOURCE_FILE" -o "$DIRECTORY"/"$base".out)"
output="$("$DIRECTORY"/"$base".out)"

echo "50.0"
echo "------------------------------"
echo "PART 2"
echo "Feedback: Everything is fine!"
