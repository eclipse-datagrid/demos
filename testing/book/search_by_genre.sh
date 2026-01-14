#!/bin/sh

# Calls the search book by genre endpoint.
#
# Usage: search_by_genre.sh <genre>[,genre,...]
# Simple Example: search_by_genre.sh horror
# Multi  Example: search_by_genre.sh horror,action
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify a search term. Example: $0 \"horror,action\"" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}
curl "$url/book/genre?genres=$1"
