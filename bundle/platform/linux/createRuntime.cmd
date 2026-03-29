:: ------------------------------------------------------------
:: Create Java runtime for Linux under Windows using Docker
:: ------------------------------------------------------------

@echo off
docker build -t codestore-linux-build ./platform/linux
docker run --rm -v ./target:/bundle/target codestore-linux-build /bundle/platform/linux/createRuntime.sh