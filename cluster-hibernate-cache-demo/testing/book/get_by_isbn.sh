#!/bin/sh

# Calls the get book by isbn endpoint.
#
# Usage: get_by_isbn.sh <book-isbn>
# Example: get_by_isbn.sh 2864330083
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify a book ISBN. Example: $0 2864330083" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}
curl "$url/book/isbn/$1"
