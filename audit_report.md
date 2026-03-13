# Dependency License Audit

This document outlines the findings of a license audit performed on the bundled dependencies within the decompiled Android application to identify any parasitic/copyleft licenses (like GPL, AGPL) that may violate the terms of typical proprietary distribution.

## Java/Kotlin Libraries
The application bundles a variety of common open-source Java/Kotlin libraries, including:
- Google Play Services / Firebase (`com.google`, `com.google.firebase`, `com.google.android.gms`) - Apache 2.0
- Facebook SDK (`com.facebook`) - MIT / Apache
- Amazon Web Services (`com.amazon`) - Apache 2.0
- Connect SDK (`com.connectsdk`) - Apache 2.0
- Ad Networks (AdColony, AppLovin, ironSource, Vungle, Tapjoy, Pollfish, IAB) - Various standard permissive SDK licenses.
- SpongyCastle (`org.spongycastle`) - MIT / Bouncy Castle License
- OkHttp (`okhttp3`) - Apache 2.0
- WebRTC (`org.webrtc`) - BSD 3-Clause

These libraries do not typically pose a risk of copyleft viral infection to the proprietary codebase.

## Native Libraries (JNI/C/C++)
The application includes several native `.so` libraries for the `arm64-v8a` architecture under `split_config.arm64_v8a.apk/lib/arm64-v8a/`. The key finding is related to **FFmpeg**.

### 1. FFmpegKit (`libffmpegkit_abidetect.so` and `com.arthenica.ffmpegkit.*` classes)
The application is using [FFmpegKit](https://github.com/arthenica/ffmpeg-kit), a wrapper around the FFmpeg library.

**Finding: HIGH RISK - POTENTIAL GPL VIOLATION**
The specific build of `libffmpegkit_abidetect.so` included in the application was compiled with the following flags (extracted from the binary):

`--enable-libx264 --enable-gpl --enable-libxvid --enable-gpl --enable-libx265 --enable-gpl --enable-libvidstab --enable-gpl --enable-zlib --enable-mediacodec`

The presence of the `--enable-gpl` flag and the inclusion of `libx264`, `libx265`, `libxvid`, and `libvidstab` means that the compiled FFmpeg library is licensed under the **GNU General Public License (GPL) version 2 or 3**.

*   `base.apk/res/raw/license_x264.txt` (GPL v2)
*   `base.apk/res/raw/license_x265.txt` (GPL v2)
*   `base.apk/res/raw/license_xvidcore.txt` (GPL v2)
*   `base.apk/res/raw/license_libvidstab.txt` (GPL v2 or later)
*   `base.apk/res/raw/license.txt` (GPL v3 text)

**Impact:**
Because the application links against and distributes a GPL-licensed version of FFmpeg, the **entire application** is subject to the terms of the GPL. This requires the source code of the proprietary application to be made available to users under the GPL, which is highly likely to be a violation of the developer's intent for a closed-source commercial application. Distributing a closed-source app linking a GPL-enabled FFmpeg on app stores is a well-known license violation.

### 2. Mozilla GeckoView / NSS libraries
The application includes libraries associated with Mozilla's Gecko browser engine or NSS (Network Security Services):
- `libxul.so`, `libmozavcodec.so`, `libmozavutil.so`, `libmozglue.so`, `libplugin-container.so`
- `libnss3.so`, `libnssckbi.so`, `libfreebl3.so`, `libsoftokn3.so`, `libipcclientcerts.so`
- `liblgpllibs.so`

**Finding: LOW/MODERATE RISK**
These libraries are typically licensed under the Mozilla Public License (MPL) 2.0 or GNU Lesser General Public License (LGPL). For example, `libmozavcodec.so` explicitly states "libavcodec license: LGPL version 2.1 or later".

**Impact:**
The MPL 2.0 and LGPL are "weak copyleft" licenses. They allow linking with proprietary software without forcing the proprietary software to become open-source, provided certain conditions are met (e.g., dynamic linking, which is the case here with `.so` files, and providing attribution/ability to swap the LGPL library). While not a viral copyleft like the GPL, the developers must ensure they meet the specific requirements of the MPL/LGPL, such as providing necessary copyright notices and source code *for the modified LGPL/MPL libraries themselves* if any modifications were made.

## Conclusion
The most critical issue is the inclusion of a GPL-compiled FFmpegKit library due to `libx264`, `libx265`, `libxvid`, and `libvidstab`. To avoid violating the GPL and keeping the application proprietary, the developers MUST replace this build of FFmpegKit with one that is compiled strictly under the LGPL (without `--enable-gpl` and excluding those specific GPL-only encoders/filters).
