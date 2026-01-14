#!/bin/sh

# Calls the list genre endpoint.
#
# Usage: list.sh
#
# To change the url set the environment variable CLUSTER_URL
# Example: export CLUSTER_URL=https://my-url.com

url=${CLUSTER_URL:=http://localhost:8080}
curl "$url/genre"
