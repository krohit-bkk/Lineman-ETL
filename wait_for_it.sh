#!/bin/sh
# wait-for-it.sh

# Usage: wait-for-it.sh host:port [-t timeout] [-- command args]

# Wait until a service is available
wait_for_service() {
  local host="$1"
  local port="$2"
  local timeout="$3"
  
  while ! nc -z "$host" "$port"; do
    if [ "$timeout" -le 0 ]; then
      echo "Timed out waiting for $host:$port to become available"
      exit 1
    fi
    sleep 1
    timeout=$((timeout - 1))
  done
}

# Parse command line arguments
host_port="$1"
timeout="${2:-15}"

host=$(echo "$host_port" | cut -d: -f1)
port=$(echo "$host_port" | cut -d: -f2)

wait_for_service "$host" "$port" "$timeout"

if [ $# -gt 2 ]; then
  shift 2
  exec "$@"
fi
