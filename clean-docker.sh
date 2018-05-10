docker ps --filter "status=exited" | grep 's ago' | awk '{print $1}' | xargs --no-run-if-empty docker rm
