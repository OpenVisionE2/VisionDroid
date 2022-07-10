#!/bin/sh

VNAME=`cat app/build.gradle | grep "versionName=" | cut -d "=" -f2 | sed 's|"||g'`

echo -e "${VNAME}"

#cd app/build/outputs/apk/google/debug
#mv -f app-google-universal-debug.apk VisionDroid-${$VNAME}.apk
#cd

#git add VisionDroid-${VNAME}.apk
git add *
git commit -m "Publish latest apk build"

