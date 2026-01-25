#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 2 ]; then
  echo "Usage: $0 <jdk-path> <javafx-path>"
  exit 1
fi

JDK_PATH=$1
JAVAFX_PATH=$2

OUTPUT_PATH=target/application/runtime
MODULE_PATH=$JDK_PATH/jmods:$JAVAFX_PATH

MODULES=\
jdk.localedata,\
java.logging,\
java.net.http,\
java.management,\
java.naming,\
java.security.jgss,\
java.instrument,\
javafx.graphics,\
javafx.controls,\
javafx.fxml,\
javafx.web

if [ -x "$JAVA_HOME/bin/jlink" ]; then
  JLINK=$JAVA_HOME/bin/jlink
else
  echo "jlink not found in JAVA_HOME"
  exit 1
fi

if [ -d "$OUTPUT_PATH" ]; then
  rm -rf "$OUTPUT_PATH"
fi

"$JLINK" \
  --module-path "$MODULE_PATH" \
  --add-modules "$MODULES" \
  --include-locales en,de \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --output "$OUTPUT_PATH"
