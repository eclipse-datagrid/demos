#!/bin/sh

# Calls the batch delete books endpoint.
#
# Usage: delete_batch.sh <book-id,book-id...>
# Example: delete_batch.sh 73d33fd3-e70d-4c46-bb31-1acbb3f2647f,3fa85f64-5717-4562-b3fc-2c963f66afa6
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify a comma-separated book ID list. Example: $0 73d33fd3-e70d-4c46-bb31-1acbb3f2647f,3fa85f64-5717-4562-b3fc-2c963f66afa6" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}

curl -X DELETE "$url/book/batch?ids=$1"
