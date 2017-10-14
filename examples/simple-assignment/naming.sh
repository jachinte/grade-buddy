#!/bin/bash
DIRECTORY=$1
REGEX="Id\:\s(V[0-9]+)"

# Usage:
#
# Capture regex for each line in file:
# 	$ cat filename | regex '.*'
# Capture 1st regex capture group for each line in file
#
# 	$ Capture 1st regex capture group for each line in file
#
# From: https://stackoverflow.com/a/14085682/738968
function regex {
	gawk 'match($0,/'$1'/, ary) {print ary['${2:-'0'}']}';
}

ANY_FILE="$(find "$DIRECTORY" -name "*.c" | head -n 1)"
ID="$(cat "$ANY_FILE" | regex "$REGEX" 1)"
echo $ID
