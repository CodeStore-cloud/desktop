:: ------------------------------------------------------------
:: Create Debian package under Windows using Docker
:: ------------------------------------------------------------

@echo off
docker build -t codestore-deb-builder .
docker run --rm -v ./target:/bundle/target codestore-linux-build /bundle/platform/linux/createPackage.sh %1