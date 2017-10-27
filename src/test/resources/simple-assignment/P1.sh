#!/bin/bash
DIRECTORY=$1
SOURCE_FILE="$(find "$DIRECTORY" -name '*P1.c' | head -n 1)"

# Set the submission as new working directory
cd $DIRECTORY

# Compile the source code
# From: https://stackoverflow.com/a/20796575/738968
name=${SOURCE_FILE##*/}
base=${name%.c}
compilation="$(gcc "$SOURCE_FILE" -o "$base".out)"
output="$(./"$base".out)"

echo $SOURCE_FILE
echo "50.0"
echo "everything is fine!"
echo $output
# >&2 echo "example error message"
# exit 1
