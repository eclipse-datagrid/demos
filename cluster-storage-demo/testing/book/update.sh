#!/bin/sh

# Calls the update book endpoint.
#
# Usage: update.sh <book-id> <updated-fields>
# Example: update.sh 73d33fd3-e70d-4c46-bb31-1acbb3f2647f '{"isbn":"2864330083","title":"Mysteries and other things","description":"This is a book about mysteries and other things","pages":305,"genres":["thriller","action"],"publicationDate":"2025-02-01"}'
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify an author ID. Example: $0 73d33fd3-e70d-4c46-bb31-1acbb3f2647f '{\"isbn\":\"2864330083\",\"title\":\"Mysteries and other things\",\"description\":\"This is a book about mysteries and other things\",\"pages\":305,\"genres\":[\"thriller\",\"action\"],\"publicationDate\":\"2025-02-01\"}'" 2>&1
  exit 1
fi

if [ -z "$2" ]; then
  echo "Please specify update fields. Example: $0 73d33fd3-e70d-4c46-bb31-1acbb3f2647f '{\"isbn\":\"2864330083\",\"title\":\"Mysteries and other things\",\"description\":\"This is a book about mysteries and other things\",\"pages\":305,\"genres\":[\"thriller\",\"action\"],\"publicationDate\":\"2025-02-01\"}'" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}
curl -X PUT -H "Content-Type:application/json" -d "$2" "$url/book/$1"
