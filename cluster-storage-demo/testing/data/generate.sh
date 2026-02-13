#!/bin/sh

# Calls the generate data endpoint.
#
# Usage: generate.sh <config>
# Example: generate.sh '{"genreConf":{"count":10},"authorConf":{"count":150},"bookConf":{"count":10000}}'
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

if [ -z "$1" ]; then
  echo "Please specify a data config. Example: $0 '{\"genreConf\":{\"count\":10},\"authorConf\":{\"count\":150},\"bookConf\":{\"count\":10000}}'" 2>&1
  exit 1
fi

url=${CLUSTER_URL:=http://localhost:8080}
curl -X POST -H "Content-Type:application/json" -d "$1" "$url/data/generate"
