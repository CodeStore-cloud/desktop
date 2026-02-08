#!/bin/bash
./startCore.sh &
./runtime/bin/java -jar ./client/CodeStoreClient.jar &