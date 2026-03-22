import os
import re

STOLEN_ASSETS = [
    "libffmpegkit_abidetect.so",
    "old.html",
    "warning-screen.html",
    "warning.png",
    "play-anywhere.html",
    "xSDK client.js"
]

def is_stolen(path):
    for asset in STOLEN_ASSETS:
        if asset in path:
            return True
    return False

def get_likely_purpose(path):
    lower_path = path.lower()

    # Specific known components from memory/function mapping
    if "com/studio08/xbgamestream/authenticate" in lower_path:
        return "Manage Xbox Live login interfaces and authentication tokens."
    if "com/studio08/xbgamestream/web" in lower_path:
        return "Rendering Microsoft OAuth login pages and interfacing with Xbox web APIs."
    if "com/studio08/xbgamestream/controller" in lower_path:
        return "Interface with Android's InputDevice APIs to read physical gamepad inputs."
    if "com/studio08/xbgamestream/controllersetup" in lower_path:
        return "Configuration and mapping setup for connected physical and virtual input devices."
    if "com/studio08/xbgamestream/servers" in lower_path:
        return "Built-in local HTTP or UPnP/DLNA servers for hosting local files for casting."
    if "com/studio08/xbgamestream/timers" in lower_path:
        return "Implement logic for verifying application purchases, licenses, or premium status unlock mechanisms."
    if "com/studio08/xbgamestream/widgets" in lower_path:
        return "Android Home Screen widgets enabling basic console power management and media remote commands."
    if "com/studio08/xbgamestream/ui" in lower_path:
        return "Visual components presenting views for streaming video, touch controls, and media casting interfaces."
    if "com/studio08/xbgamestream/converter" in lower_path:
        return "Components that manage the transcoding or conversion of local media files."
    if "com/studio08/xbgamestream/cast" in lower_path:
        return "Integrations for media casting protocols and active casting sessions."
    if "crypto" in lower_path:
        return "Executes Elliptic Curve Diffie-Hellman (ECDH) key exchange and AES-CBC encryption/decryption for SmartGlass."
    if "network" in lower_path:
        return "Background service bridging the application UI with the SmartGlass and Nano clients."
    if "nano/base" in lower_path:
        return "Core Nano gamestream client managing UDP/TCP sockets and decoding incoming H.264 video streams."
    if "nano/streams" in lower_path:
        return "Handlers for multiplexed streams separating control packets from video and input data."
    if "nano/packets" in lower_path:
        return "Packet definition classes for serializing handshakes, control messages, and controller input frames."
    if "studio09/gameconrtollerforxbox" in lower_path:
        return "Standalone activity supplying a digital gamepad interface."
    if "interfaces" in lower_path:
        return "Define event signatures for asynchronous communication between protocols."
    if "util" in lower_path:
        return "Utility functions for byte array manipulation, UUID processing, endianness conversions."
    if "channels" in lower_path:
        return "Processing specific subsets of SmartGlass communications (text entry, media remote)."
    if "xbox" in lower_path:
        return "Main SmartGlass client executing console discovery, connection, and channel management."
    if "packet" in lower_path:
        return "Structuring, serializing, and deserializing binary packets for the SmartGlass protocol."
    if "constants" in lower_path:
        return "House fixed byte sequences, flags, and mappings translating inputs into binary formats."

    # Specific individual files
    if path.endswith("background.js"):
        return "Content script acting as a messaging bridge between the Android host and the GeckoView web view."
    if path.endswith("background.test.js"):
        return "Standalone test suite to verify the behavior of the background.js messaging bridge."
    if path.endswith("omsdk_v_1_0.js"):
        return "IAB Open Measurement SDK for ad viewability measurement."
    if path.endswith("MainActivity.java"):
        return "Minimal placeholder entry point in the reconstructed app for loading native JNI libraries."

    # General categorization based on file type / location
    if path.endswith(".apk"):
        return "Decompiled Android application package (or split package) containing compiled code, resources, and assets."
    if "res/raw" in lower_path:
        return "Raw binary or text resources included in the application."
    if "res/layout" in lower_path:
        return "XML layout definition files for the application's user interface."
    if "res/drawable" in lower_path or path.endswith((".png", ".jpg", ".webp")):
        return "Image or graphical resources used by the application."
    if "res/values" in lower_path:
        return "XML resource files defining strings, colors, styles, or dimensions."
    if path.endswith(".xml"):
        return "XML configuration or layout file."
    if path.endswith(".so"):
        return "Compiled native shared library (JNI/C/C++) for specific architecture."
    if path.endswith(".smali"):
        return "Dalvik bytecode instruction file representing decompiled Java code."
    if path.endswith(".java"):
        return "Java source code file."
    if path.endswith(".js"):
        return "JavaScript source code or web asset file."
    if "assets/" in lower_path:
        return "Application asset file used for bridging, web views, or bundled web content."
    if "meta-inf" in lower_path:
        return "Application signature, certificate, and manifest metadata files."
    if path.endswith(".md") or path.endswith(".txt"):
        return "Documentation or informational text file."

    return "Undocumented component likely part of the decompiled application structure."

def main():
    with open("file_list.txt", "r", encoding="utf-8") as f:
        files = f.read().splitlines()

    mapping = [
        "Xbox ONE Game Streaming App - Master File Functionality Map",
        "===========================================================",
        "",
        "This document provides a comprehensive classification of the individual files and components within the application to detail their likely purpose. Open-source libraries flagged as copyleft violations or stolen assets have been explicitly excluded.",
        ""
    ]

    files.sort()

    for file_path in files:
        if file_path.startswith("./"):
            file_path = file_path[2:]

        if not file_path:
            continue

        if is_stolen(file_path):
            continue

        # Ignore some common git/build files
        if file_path.startswith(".git") or file_path in ["file_list.txt", "dir_list.txt"]:
            continue

        purpose = get_likely_purpose(file_path)
        mapping.append(f"- `{file_path}`: {purpose}")

    with open("master_mapping.txt", "w", encoding="utf-8") as f:
        f.write("\n".join(mapping) + "\n")

if __name__ == "__main__":
    main()
