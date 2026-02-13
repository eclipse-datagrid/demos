#!/bin/sh

# Calls the search author by name endpoint.
#
# Usage: search_by_name.sh <search-term>
# Example: search_by_name.sh "Jo"
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify a search term. Example: $0 Jo" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}
curl "$url/author/name?search=$1"
