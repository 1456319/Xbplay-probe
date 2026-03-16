# Stolen Asset Report

## File Hashes and Proprietary Strings

### old.html
- **SHA-256:** `354f56b45478497c045a7423d9bb2ff98629fa06fef6d6ba758613794b647bf9`
- **CRITICAL:** Contains proprietary Microsoft Xbox Cloud Gaming API endpoint (`gssv-play-prodxhome.xboxlive.com`). This strongly indicates unlicensed use of Microsoft's streaming infrastructure.
- **CRITICAL:** Contains logging strings referencing `xSDK client.js`, suggesting direct copying of Xbox streaming client logic.

### warning-screen.html
- **SHA-256:** `e776fb008320666cf1018fb36064713d7a72e9be024506e38920b19d5854c9d3`

### warning.png
- **SHA-256:** `29eeed523e20a3e8b317e9e0e08b2df93c10e6bc2357250faae270490343bda5`
- Raw asset image extracted.

### play-anywhere.html
- **SHA-256:** `ec42ba27e781102e4c93bf20082ba2fec8453e88ab033870c0e6dc1cead21972`
- Contains placeholder `STREAM_VIEW_URL`.


## Cross-Reference with Manifests

The embedded copyright strings and API endpoints pointing directly to `xboxlive.com` heavily imply the unauthorized use of Microsoft Xbox Cloud Gaming SDKs or web client code. These assets and endpoints are NOT covered by the provided `license.txt`, `license_ffmpeg.txt` or standard open-source licenses bundled with the app.

The decompiled logic and references to `xSDK` clearly indicate this application acts as a thin client wrapping proprietary, unlicensed Microsoft code for game streaming.
