#!/bin/bash

aws s3 sync . s3://bytmap.sk/ --acl public-read --exclude upload.sh
