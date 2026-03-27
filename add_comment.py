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

comment_old_html = "<!-- This file acts as the front-end web asset containing Microsoft Xbox Cloud Gaming API endpoints (e.g., gssv-play-prodxhome.xboxlive.com) and client logic (xSDK client.js). Its functions include establishing WebRTC peer connections, handling ICE candidate negotiation, creating data channels (video, audio, input, control, message, chat), and managing media source rendering. -->"

add_comment('base.apk/assets/old.html', comment_old_html, '\r\n')
add_comment('extracted_assets/old.html', comment_old_html, '\n')
