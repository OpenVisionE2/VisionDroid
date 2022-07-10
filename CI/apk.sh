#!/bin/sh

# Script by Persian Prince for https://github.com/OpenVisionE2
# You're not allowed to remove my copyright or reuse this script without putting this header.

VNAME=`cat app/build.gradle | grep "versionName=" | cut -d "=" -f2 | sed 's|"||g'`

mkdir -p apk
rm -f apk/*.apk
mv -f app/build/outputs/apk/google/debug/app-google-universal-debug.apk apk/VisionDroid-${VNAME}.apk
rm -rf app/build

git add -u
git add *
git commit -m "Publish latest ${VNAME} apk file"
