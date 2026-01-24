#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <version>"
  exit 1
fi

VERSION=$1
BUILD_DIR=../target/deb
DEBIAN=$BUILD_DIR/DEBIAN
USR_BIN=$BUILD_DIR/usr/bin
USR_SHARE_CODESTORE=$BUILD_DIR/usr/share/codestore
USR_SHARE_APPLICATIONS=$BUILD_DIR/usr/share/applications
USR_SHARE_ICONS=$BUILD_DIR/usr/share/icons/hicolor/64x64/apps
WRAPPER_SCRIPT=$USR_BIN/codestore

echo "Creating folder structure ..."
mkdir -p $BUILD_DIR
mkdir -p \
  $DEBIAN \
  $USR_BIN \
  $USR_SHARE_CODESTORE \
  $USR_SHARE_APPLICATIONS \
  $USR_SHARE_ICONS

echo "Copying files ..."
sed "s/@VERSION@/$VERSION/" control > $DEBIAN/control
cp codestore.desktop $USR_SHARE_APPLICATIONS
cp icon.png $USR_SHARE_ICONS
cp -r ../target/application/* $USR_SHARE_CODESTORE

echo "Creating wrapper script ..."
cat > $WRAPPER_SCRIPT <<EOF
#!/usr/bin/env bash
cd /usr/share/codestore
exec ./CodeStore.sh
EOF
chmod +x $WRAPPER_SCRIPT

echo "Building Debian package ..."
dpkg-deb --build $BUILD_DIR
mv $BUILD_DIR/codestore.deb "codestore_${VERSION}_amd64.deb"

echo "Package created successfully"
