#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Error: No version specified"
  exit 1
fi

cd "$(dirname "${BASH_SOURCE[0]}")" #/bundle/platform/linux

VERSION=$1
TARGET=/bundle/target
SNAP_DIR=$TARGET/snap
CODESTORE_DIR=$SNAP_DIR/codestore

echo "Creating folder structure ..."
mkdir -p $CODESTORE_DIR
cp -r ./icon.png $CODESTORE_DIR
cp -r ./codestore.desktop $CODESTORE_DIR
cp -r $TARGET/application/* $CODESTORE_DIR

cat << EOF > $SNAP_DIR/snapcraft.yaml
name: {CodeStore}
base: core24
version: "$VERSION"
summary: Developer tool for managing code snippets
description: |
  {CodeStore} is a free code snippet manager that makes it easy for programmers to store and organize code snippets.
  It provides syntax highlighting, tagging and full text search.
icon: codestore/icon.png
grade: stable
confinement: strict

apps:
  codestore:
    command: codestore/codestore.sh
    desktop: codestore/codestore.desktop
    plugs:
      - home
      - network
      - network-bind
      - desktop
      - desktop-legacy
      - opengl
      - wayland
      - x11

parts:
  application:
    plugin: dump
    source: .
EOF

echo "Building Snap package ..."
cd $SNAP_DIR
snapcraft --destructive-mode
echo "Package created successfully"
