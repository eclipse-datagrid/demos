#!/bin/sh

# Calls the insert book endpoint.
#
# Usage: insert.sh <books>
# Example: insert.sh '[{"isbn":"2864330083","title":"Mysteries and other things","description":"This is a book about mysteries and other things","pages":305,"genres":["thriller","action"],"publicationDate":"2025-02-01","authorId":"73d33fd3-e70d-4c46-bb31-1acbb3f2647f"}]'
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify a book list. Example: $0 '[{\"isbn\":\"2864330083\",\"title\":\"Mysteries and other things\",\"description\":\"This is a book about mysteries and other things\",\"pages\":305,\"genres\":[\"thriller\",\"action\"],\"publicationDate\":\"2025-02-01\",\"authorId\":\"73d33fd3-e70d-4c46-bb31-1acbb3f2647f\"}]'" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}
curl -H "Content-Type:application/json" -d "$1" "$url/book"
