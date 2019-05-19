#!/bin/bash
echo ">> Publishing to Google Play Store...";

PKG_NAME="org.mtransit.android";
echoe "PKG_NAME: $PKG_NAME";

APK_FILE_PATH="../app-android/build/outputs/apk/release/*-release.apk";
echoe "APK_FILE_PATH: APK_FILE_PATH";

../gradlew :play-publisher:run --args="$PKG_NAME $APK_FILE_PATH";
RESULT=$?;

echo ">> Publishing to Google Play Store... DONE";
exit ${RESULT};

