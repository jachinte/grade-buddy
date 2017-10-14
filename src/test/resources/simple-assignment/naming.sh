#!/bin/bash
DIRECTORY=$1
ID_REGEX="V[0-9]+"
REGEX="Id\:\s("$ID_REGEX")"

ANY_FILE="$(find "$DIRECTORY" -name "*.c" | head -n 1)"
ID="$(cat "$ANY_FILE" | grep -oEi "$REGEX" | grep -oEi "$ID_REGEX")"
echo $ID
