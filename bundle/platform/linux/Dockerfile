FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt update
RUN apt install -y software-properties-common squashfs-tools sudo git curl snapd
RUN snap install snapcraft --classic

WORKDIR /bundle/platform/linux
COPY . /bundle/platform/linux
RUN chmod +x createRuntime.sh
RUN chmod +x createPackage.sh