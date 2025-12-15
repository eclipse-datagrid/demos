#!/bin/sh

# Calls the delete book endpoint.
#
# Usage: delete.sh <book-id>
# Example: delete.sh 73d33fd3-e70d-4c46-bb31-1acbb3f2647f
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify an book ID. Example: $0 73d33fd3-e70d-4c46-bb31-1acbb3f2647f" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}

curl -X DELETE "$url/book/$1"
