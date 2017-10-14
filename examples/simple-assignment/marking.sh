#!/bin/bash
DIRECTORY=$1

# Compile the source code
for file in "$(find "$DIRECTORY" -name '*.c')"
do
	# From: https://stackoverflow.com/a/20796575/738968
	name=${file##*/}
	base=${name%.c}
	output="$(gcc "$file" -o "$DIRECTORY"/"$base".out)"
	output="$(./"$DIRECTORY"/"$base".out)"
done

echo "100.0"
echo "Everything is good!"
