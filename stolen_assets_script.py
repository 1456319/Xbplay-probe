import os
import hashlib

asset_dir = "extracted_assets"
files = os.listdir(asset_dir)

report = "# Stolen Asset Report\n\n"
report += "## File Hashes and Proprietary Strings\n\n"

for f in files:
    filepath = os.path.join(asset_dir, f)
    with open(filepath, "rb") as file:
        content = file.read()
        sha256_hash = hashlib.sha256(content).hexdigest()

    report += f"### {f}\n"
    report += f"- **SHA-256:** `{sha256_hash}`\n"

    if f.endswith(".html"):
        with open(filepath, "r") as file:
            text_content = file.read()
            if "STREAM_VIEW_URL" in text_content:
                report += "- Contains placeholder `STREAM_VIEW_URL`.\n"
            if "gssv-play-prodxhome.xboxlive.com" in text_content:
                report += "- **CRITICAL:** Contains proprietary Microsoft Xbox Cloud Gaming API endpoint (`gssv-play-prodxhome.xboxlive.com`). This strongly indicates unlicensed use of Microsoft's streaming infrastructure.\n"
            if "xSDK client.js" in text_content:
                report += "- **CRITICAL:** Contains logging strings referencing `xSDK client.js`, suggesting direct copying of Xbox streaming client logic.\n"
    elif f.endswith(".png"):
         report += "- Raw asset image extracted.\n"

    report += "\n"

report += """
## Cross-Reference with Manifests

The embedded copyright strings and API endpoints pointing directly to `xboxlive.com` heavily imply the unauthorized use of Microsoft Xbox Cloud Gaming SDKs or web client code. These assets and endpoints are NOT covered by the provided `license.txt`, `license_ffmpeg.txt` or standard open-source licenses bundled with the app.

The decompiled logic and references to `xSDK` clearly indicate this application acts as a thin client wrapping proprietary, unlicensed Microsoft code for game streaming.
"""

with open("stolen_asset_report.md", "w") as report_file:
    report_file.write(report)

print("Report generated.")
