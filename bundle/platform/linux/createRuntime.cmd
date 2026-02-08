:: ------------------------------------------------------------
:: Create Java runtime for Linux under Windows using Docker
:: ------------------------------------------------------------

@echo off
docker build -t codestore-linux-build .
docker run --rm -v ./target:/bundle/target codestore-linux-build /bundle/platform/linux/createRuntime.sh