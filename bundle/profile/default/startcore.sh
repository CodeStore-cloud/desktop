#!/bin/bash
if [ ! -f "$HOME/CodeStore/core-api-url" ]; then
    ./runtime/bin/java -jar ./core/CodeStoreCore.jar &
fi