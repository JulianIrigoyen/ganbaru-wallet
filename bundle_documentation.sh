#!/usr/bin/env bash

swagger-cli bundle docs/api_v1.yaml --outfile _build/openapi3.yaml --type yaml
redoc-cli bundle _build/openapi3.yaml -o "public/docs/index.html"
rm -rf _build
#open public/docs/api_v1.html