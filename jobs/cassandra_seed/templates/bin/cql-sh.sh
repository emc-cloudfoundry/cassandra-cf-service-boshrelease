#!/bin/bash

set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.

export LANG=en_US.UTF-8

export CASSANDRA_BIN=/var/vcap/packages/cassandra/bin
export CASSANDRA_CONF=/var/vcap/jobs/cassandra_server/conf

export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:/var/vcap/packages/openjdk/bin:$CASSANDRA_BIN:$CASSANDRA_CONF

## export CASSANDRA_CONF=/var/vcap/jobs/cassandra_server/conf

pushd /var/vcap/packages/cassandra/bin
exec chpst -u vcap:vcap /var/vcap/packages/cassandra/bin/cqlsh
popd
exit 0
