#!/bin/sh

# Calls the insert genre endpoint.
#
# Usage: insert.sh <genre>
# Example: insert.sh thriller
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify a genre name. Example: $0 thriller" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}
curl -X PUT "$url/genre/$1"
