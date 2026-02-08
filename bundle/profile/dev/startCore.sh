#!/bin/bash
if [ ! -f "./data/core-api-url" ]; then
    ./runtime/bin/java -Dspring.profiles.active=dev -jar ./CodeStoreCore.jar &
fi