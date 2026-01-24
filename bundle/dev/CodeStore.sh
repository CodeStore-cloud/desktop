#!/bin/bash
./startCore.sh &
./runtime/bin/java -Dspring.profiles.active=dev -jar ./client/CodeStoreClient.jar &