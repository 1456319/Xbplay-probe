# Android Rebuild Troubleshooting Document

## Overview
This document logs the errors encountered and solutions applied during Phase 1 & 2 of the Android app reconstruction for Xbgamestream.

## Missing Resources
- Issue: `$mute_to_unmute__3.xml` contains invalid character `$`
- Solution: Removed invalidly named XML files from the `res` directory. In a full rebuild, these files would need to be correctly named or recreated.

## Android Manifest Conflicts
- Issue: Original `AndroidManifest.xml` had missing `mipmap/ic_launcher` resources and custom attributes that failed resource linking.
- Solution: A minimal placeholder `AndroidManifest.xml` was created to allow the build process to proceed. This isolates the native JNI/Gradle configuration from resource-heavy UI issues.

## Gradle Dependency Conflicts
- Issue: Missing Jetifier/AndroidX flags.
- Solution: Added `android.useAndroidX=true` and `android.enableJetifier=true` to `gradle.properties`.

## APK Assembly Conflict
- Issue: Duplicate `assets/dexopt/baseline.prof` file found during `packageRelease` or `assembleDebug`.
- Solution: Added `pickFirst 'assets/dexopt/baseline.prof'` to `packagingOptions` in the `app/build.gradle` file.

## Expected Native Link Errors
- The placeholder `MainActivity` attempts to load `gkcodecs` (`System.loadLibrary("gkcodecs")`).
- `UnsatisfiedLinkError` is anticipated at runtime as Java Native Interfaces (JNI) are not yet implemented. Native libraries (.so) have successfully been mapped and included in the `jniLibs/arm64-v8a` directory.
