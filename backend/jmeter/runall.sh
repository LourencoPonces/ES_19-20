#!/bin/bash

# exit whole script when Ctrl+C is pressed (instead of stopping current test)
trap "exit" INT

for test in **/*.jmx; do
	echo "Running test: $test"
	./runone.sh $test $@
	echo
done
