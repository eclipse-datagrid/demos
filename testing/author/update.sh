#!/bin/sh

# Calls the update author endpoint.
#
# Usage: update.sh <author-id> <updated-fields>
# Example: update.sh 73d33fd3-e70d-4c46-bb31-1acbb3f2647f '{"name":"John Update Doe","about":"The worlds most-updated author."}'
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify an author ID. Example: $0 73d33fd3-e70d-4c46-bb31-1acbb3f2647f '{\"name\":\"John Update Doe\",\"about\":\"The worlds most-updated author.\"}'" 2>&1
  exit 1
fi

if [ -z "$2" ]; then
  echo "Please specify update fields. Example: $0 73d33fd3-e70d-4c46-bb31-1acbb3f2647f '{\"name\":\"John Update Doe\",\"about\":\"The worlds most-updated author.\"}'" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}
curl -X PUT -H "Content-Type:application/json" -d "$2" "$url/author/$1"
