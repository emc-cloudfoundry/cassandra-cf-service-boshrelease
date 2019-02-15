#!/usr/bin/env bash

<%
  require "shellwords"

  def esc(x)
      Shellwords.shellescape(x)
  end
%>

# set -x # Print commands and their arguments as they are executed.
set -u # report the usage of uninitialized variables.

function display_exit_code() {
    local exit_code=$?

    if [ $exit_code -eq 0 ]; then
        echo "BOSH post-deploy script succeeded"
    else
        echo "BOSH post-deploy script failed, with exit status: '$exit_code'"
    fi
}

trap display_exit_code EXIT

function prepend_datetime() {
    awk -W interactive '{ system("echo -n [$(date +%FT%T%z)]"); print " " $0 }'
}

exec \
    3>&1 \
    4>&2 \
    > >(prepend_datetime >&3) \
    2> >(prepend_datetime >&4)

export LANG=en_US.UTF-8

job_dir=/var/vcap/jobs/cassandra

export CASSANDRA_BIN=/var/vcap/packages/cassandra/bin
export CASSANDRA_CONF=$job_dir/conf

export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=/var/vcap/packages/python/usr/bin/:$PATH:$JAVA_HOME/bin:$CASSANDRA_BIN


function log_err() {
    echo "$@" >&2
}


node_id=$(/var/vcap/jobs/cassandra/bin/nodetool info \
            | awk -F: '/^ID/{print $2}' | tr -d ' ')

up_node_with_lowest_id=$(/var/vcap/jobs/cassandra/bin/nodetool status \
            | grep "^U[NLJM] " | sort -k7 | head -n1 | awk '{print $7}')

if [[ $node_id != $up_node_with_lowest_id ]]; then
    echo "INFO: current node '$node_id' is not lowest ID of all UP nodes" \
        "(i.e. '$up_node_with_lowest_id') according to 'nodetool'." \
        "Aborting the 'post-deploy' execution on this node."
    exit 0
fi

max_attempts=120
cass_ip=<%= esc(spec.ip) %>
cass_port=<%= esc(p('native_transport_port')) %>
attempts=0
while ! nc -z "$cass_ip" "$cass_port"; do
    attempts=$(($attempts + 1))
    if [[ $attempts -ge $max_attempts ]]; then
        log_err "ERROR: could not reach cassandra on IP '$cass_ip' and TCP port '$cass_port'" \
             "after '$max_attempts' attemps. Aborting."
        exit 1
    fi
    sleep 1
done
echo "INFO: reached Cassandra on '$cass_ip:$cass_port' after '$(($attempts + 1))' attemps."
if [[ $attempts -gt 0 ]]; then
    echo "INFO: Waiting 30 more seconds for the service to be available."
    sleep 30
fi



password_file=/var/vcap/store/cassandra/cassandra.secret
readonly password_file
function store_password() {
    local password=$1
    printf "$password" | base64 > "$password_file"
}

if [ ! -e "$password_file" ]; then
    store_password cassandra
fi
chown root:vcap "$password_file"
chmod 600 "$password_file"
current_password=$(cat "$password_file" | base64 --decode)
new_password=<%= esc(p('cassandra_password')) %>

log_err "INFO: setting password"
$CASSANDRA_BIN/cqlsh --cqlshrc "$job_dir/root/.cassandra/cqlshrc" \
    -u cassandra -p "$current_password" \
    -e "ALTER ROLE cassandra WITH password = '$new_password'"
failure=$?
log_err "DEBUG: setting password, exit status: '$failure'"
if [[ "$failure" != 0 ]]; then
    log_err "INFO: verifying that the current password is the desired password"
    $CASSANDRA_BIN/cqlsh --cqlshrc "$job_dir/root/.cassandra/cqlshrc" \
        -e "alter role cassandra with password = '$new_password' "
    failure2=$?
    log_err "DEBUG: verifying current password, exit status: '$failure2'"
    if [ "$failure2" != 0 ]; then
        log_err "ERROR: the password for user 'cassandra' is inconsistent. Aborting."
        exit 1
    fi
fi

store_password "$new_password"

log_err "INFO: waiting 5 secs for the cassandra password to effectively be changed"
sleep 5



log_err "INFO: setting replication strategy for cassandra password"
<%
    replication_factor = p('system_auth_keyspace_replication_factor', link('seeds').instances.count)
    if !replication_factor.is_a?(Integer)
        replication_factor = link('seeds').instances.count
    end
-%>
# Note: should we support multiple datacenters one day, then we should set the
# replication class to 'NetworkTopologyStrategy' here instead of 'SimpleStrategy'.
# See: <https://docs.datastax.com/en/cassandra/latest/cassandra/configuration/configCassandra_yaml.html#configCassandra_yaml__authenticator>
# See: <https://docs.datastax.com/en/cassandra/latest/cassandra/configuration/configCassandra_yaml.html#configCassandra_yaml__authorizer>
$CASSANDRA_BIN/cqlsh --cqlshrc "$job_dir/root/.cassandra/cqlshrc" \
    -e "ALTER KEYSPACE system_auth WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': <%= replication_factor %>}  AND durable_writes = true"

log_err "INFO: propagating any new password with the enforced replication strategy"
$job_dir/bin/nodetool repair -full system_auth

exit 0
