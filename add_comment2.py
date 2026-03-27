import sys

def add_comment(filepath, comment, newline_char):
    try:
        with open(filepath, 'rb') as f:
            content = f.read()

        if comment.encode() in content:
            print(f"Comment already present in {filepath}")
            return

        with open(filepath, 'wb') as f:
            f.write(comment.encode() + newline_char.encode() + content)
        print(f"Added comment to {filepath}")
    except FileNotFoundError:
        print(f"File not found: {filepath}")

comment_bg_js = "/* This content script acts as a messaging bridge between the Android host and the GeckoView web view. It securely transfers JSON payloads and CustomEvents, employing a recursive isValidData function to ensure only safe, plain objects and primitives are transferred via nativeMessaging, preventing unsafe data cloning (such as functions or prototypes). */"

add_comment('base.apk/assets/messaging/background.js', comment_bg_js, '\r\n')
add_comment('app/src/main/assets/messaging/background.js', comment_bg_js, '\n')
