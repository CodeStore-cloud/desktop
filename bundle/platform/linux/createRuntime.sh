#!/usr/bin/env bash
set -euo pipefail

JDK_VERSION=21.0.2
JDK_URL=https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_linux-x64_bin.tar.gz
JAVAFX_VERSION=21.0.10
JAVAFX_URL=https://download2.gluonhq.com/openjfx/21.0.10/openjfx-21.0.10_linux-x64_bin-jmods.zip

CACHE_DIR=~/.cache
JDK_PATH=$CACHE_DIR/jdk-$JDK_VERSION
JDK_JMODS=$JDK_PATH/jmods
JAVAFX_JMODS=$CACHE_DIR/javafx-jmods-$JAVAFX_VERSION

if [ ! -d $CACHE_DIR ]; then
  mkdir $CACHE_DIR
fi

# ------------------------------------------------------------
# Download OpenJDK (cached)
# ------------------------------------------------------------
if [ -d "$JDK_PATH" ]; then
  echo Using cached OpenJDK
else
  echo Downloading OpenJDK $JDK_VERSION ...
  TAR_FILE=$CACHE_DIR/jdk-$JDK_VERSION.tar.gz
  curl -o $TAR_FILE $JDK_URL
  tar -xf $TAR_FILE -C $CACHE_DIR
  rm -f $TAR_FILE
fi

# ------------------------------------------------------------
# Download JavaFX (cached)
# ------------------------------------------------------------

if [ -d $JAVAFX_JMODS ]; then
  echo Using cached JavaFX
else
  echo Downloading JavaFX $JAVAFX_VERSION jmods ...
  TAR_FILE=$CACHE_DIR/javafx-$JAVAFX_VERSION.tar.gz
  curl -L -o $TAR_FILE $JAVAFX_URL
  tar -xf $TAR_FILE -C $CACHE_DIR
  rm -f $TAR_FILE
fi

OUTPUT_PATH=target/application/runtime
MODULE_PATH=$JDK_JMODS:$JAVAFX_JMODS
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

if [ -d $OUTPUT_PATH ]; then
  rm -rf $OUTPUT_PATH
fi

$JDK_PATH/bin/jlink \
  --module-path "$MODULE_PATH" \
  --add-modules "$MODULES" \
  --include-locales en,de \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --output "$OUTPUT_PATH"
