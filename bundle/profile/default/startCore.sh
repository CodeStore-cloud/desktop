#!/bin/bash
CORE_FILE="$HOME/CodeStore/core-api-url"
if [ ! -f "$CORE_FILE" ]; then
    ./runtime/bin/java -jar ./core/CodeStoreCore.jar &
fi