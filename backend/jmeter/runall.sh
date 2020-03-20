#!/bin/bash

for test in **/*.jmx; do
	echo "Running test: $test"
	./runone.sh $test $@
	echo
done
