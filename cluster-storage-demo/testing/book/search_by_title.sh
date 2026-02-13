#!/bin/sh

# Calls the search book by title endpoint.
#
# Usage: search_by_title.sh <search-term>
# Example: search_by_title.sh "a"
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify a search term. Example: $0 \"a\"" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}
curl "$url/book/title?search=$1"
