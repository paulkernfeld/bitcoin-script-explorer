#!/bin/bash

echo "lein test-refresh"

declare -a arr=("lein cljsbuild auto testable" "lein cljsbuild auto production")

## now loop through the above array
for cmd in "${arr[@]}"; do {
  echo "Process \"$cmd\" started";
  $cmd & pid=$!
  PID_LIST+=" $pid";
} done

trap "kill $PID_LIST" SIGINT

echo "Parallel processes have started";

wait $PID_LIST

echo
echo "All processes have completed";

# disclaimer: this needs to be killed via the activity monitor

# todo: run tests in JS

