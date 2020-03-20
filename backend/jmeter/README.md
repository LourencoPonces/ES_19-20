Running JMeter tests doesn't have to be hard!

To make it easy for you to run them in different environments, some parameters can be adjusted via a properties file:
- db.url - Database Connection URL
- db.user - Database Username
- db.password - Database Password

A sample file that can be used against the dockerized Quiz Tutor is available in `jmeter-docker.properties`

To run a JMeter test with a properties file easily, use the `runonce.sh` script.
Any arguments passed to the script (except the first, which is the path to the test) will be passed to JMeter.

The `jmeter-docker.properties` file is loaded by default in every test (can be overriden by the PROPERTIES\_FILE environment variable).

If you don't have `jmeter` on your path, you can specify an alternate path to JMeter using the JMETER environment variable.

## Running all tests
If you want to run all tests sequentially, use the `runall.sh` script. All arguments passed to it will be passed to JMeter.

It simply executes `runone.sh` for every JMeter test existent, so all property loading behaviors and environment variables are inherited.
