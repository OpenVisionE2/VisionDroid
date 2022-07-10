#!/bin/sh

VNAME=`cat app/build.gradle | grep "versionName=" | cut -d "=" -f2 | sed 's|"||g'`

echo -e "{$VNAME}"

git add *.apk
git commit -m "Publish latest apk build"

