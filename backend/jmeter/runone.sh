#!/bin/bash

# this may not work if you add it to your $PATH, but that is not expected
SCRIPT_DIR="$(dirname $0)"

# path/name of jmeter binary
JMETER="${JMETER:=jmeter}"

# properties file to load
PROPERTIES_FILE="${PROPERTIES_FILE:=$SCRIPT_DIR/jmeter-docker.properties}"

if [[ $# -lt 1 ]]; then
	echo "Usage: $0 <test> [extra arguments]"
	echo 'Will run $JMETER [extra arguments] -t <test> -p $PROPERTIES_FILE'
	echo "JMETER=$JMETER"
	echo "PROPERTIES_FILE=$PROPERTIES_FILE"
	exit 1
fi

test="$1"
shift

exec $JMETER $@ -t "$test" -q "$PROPERTIES_FILE"
