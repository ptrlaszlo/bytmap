#!/bin/bash

if tsc ; then
  aws s3 sync . s3://bytmap.sk/ --acl public-read --exclude "*" --include "index.html" --include "assets/*"
fi