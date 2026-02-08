#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Error: No version specified"
  exit 1
fi

cd "$(dirname "${BASH_SOURCE[0]}")"

VERSION=$1
TARGET=../../target
DEB_DIR=$TARGET/deb
DEBIAN=$DEB_DIR/DEBIAN
USR_BIN=$DEB_DIR/usr/bin
USR_SHARE_CODESTORE=$DEB_DIR/usr/share/codestore
USR_SHARE_APPLICATIONS=$DEB_DIR/usr/share/applications
USR_SHARE_ICONS=$DEB_DIR/usr/share/icons/hicolor/64x64/apps
WRAPPER_SCRIPT=$USR_BIN/codestore

echo "Creating folder structure ..."
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
cp -r $TARGET/application/* $USR_SHARE_CODESTORE

echo "Creating wrapper script ..."
cat > $WRAPPER_SCRIPT <<EOF
#!/usr/bin/env bash
cd /usr/share/codestore
exec ./CodeStore.sh
EOF
chmod +x $WRAPPER_SCRIPT

echo "Building Debian package ..."
dpkg-deb --build $DEB_DIR $TARGET

echo "Package created successfully"
