FROM ubuntu:24.04

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y \
      build-essential \
      dpkg-dev \
      debhelper \
      fakeroot \
      wget \
      unzip \
      bash \
      coreutils \
      sed \
      gettext \
      imagemagick && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /bundle/platform/linux
COPY . /bundle/platform/linux
RUN chmod +x createRuntime.sh
RUN chmod +x createPackage.sh