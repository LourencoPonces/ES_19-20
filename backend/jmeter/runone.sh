#!/bin/bash

# this may not work if you add it to your $PATH, but that is not expected
SCRIPT_DIR="$(dirname $0)"

# path/name of jmeter binary
JMETER="${JMETER:=jmeter}"

# properties file to load
PROPERTIES_FILE="${PROPERTIES_FILE:=$SCRIPT_DIR/jmeter-docker.properties}"
PROPERTIES_FILE="$(realpath "$PROPERTIES_FILE")"

if [[ $# -lt 1 ]]; then
	echo "Usage: $0 <test> [extra arguments]"
	echo 'Will run $JMETER [extra arguments] -t <test> -p $PROPERTIES_FILE'
	echo "JMETER=$JMETER"
	echo "PROPERTIES_FILE=$PROPERTIES_FILE"
	exit 1
fi

test="$1"
shift

test_abs="$(realpath $test)"
pushd "$(dirname $test)" # ensure all resources can be properly loaded

$JMETER $@ -t "$test_abs" -p "$PROPERTIES_FILE"

popd # restore cwd
