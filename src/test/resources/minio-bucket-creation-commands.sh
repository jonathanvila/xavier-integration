#!/usr/bin/env bash
/usr/bin/mc config host add myminio http://$1 BQA2GEXO711FVBVXDWKM uvgz3LCwWM3e400cDkQIH/y1Y4xgU4iV91CwFSPC;
/usr/bin/mc mb myminio/insights-upload-perma;
/usr/bin/mc mb myminio/insights-upload-rejected;
exit 0;
