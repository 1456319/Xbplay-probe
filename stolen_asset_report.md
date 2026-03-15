# Stolen Asset Report

## File Hashes and Proprietary Strings

### old.html
- **SHA-256:** `0d1c998a3884c4c6f40b4585894bc337acc6c3057dea845e9d99f98ee02c6aec`
- **CRITICAL:** Contains proprietary Microsoft Xbox Cloud Gaming API endpoint (`gssv-play-prodxhome.xboxlive.com`). This strongly indicates unlicensed use of Microsoft's streaming infrastructure.
- **CRITICAL:** Contains logging strings referencing `xSDK client.js`, suggesting direct copying of Xbox streaming client logic.

### warning-screen.html
- **SHA-256:** `1ffc9f6b9ce60dca5637a2c0b77c13c23b1fca0ac58a9e9130e110bc628f3a80`

### warning.png
- **SHA-256:** `29eeed523e20a3e8b317e9e0e08b2df93c10e6bc2357250faae270490343bda5`
- Raw asset image extracted.

### play-anywhere.html
- **SHA-256:** `653d558e42a2a4e4e06f4619bb68682030699a3b60e35eb252d4077420abd98f`
- Contains placeholder `STREAM_VIEW_URL`.

## Cross-Reference with Manifests

The embedded strings and API endpoints pointing directly to `xboxlive.com` heavily imply the unauthorized use of Microsoft Xbox Cloud Gaming SDKs or web client code.

The provided manifest licenses (`base.apk/res/raw/license.txt` for GPLv3, `base.apk/res/raw/license_ffmpeg.txt`, etc.) cover standard open-source libraries (like FFmpeg). They **do not** cover proprietary assets or APIs owned by Microsoft Corporation. The inclusion of `xSDK client.js` code and specific endpoint routing within `old.html` points directly toward code theft.

The decompiled logic clearly indicates this application acts as a thin client wrapping proprietary, unlicensed Microsoft code for game streaming.
