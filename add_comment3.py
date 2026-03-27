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

comment_play = "<!-- This file is a web asset containing a placeholder STREAM_VIEW_URL. -->"

add_comment('extracted_assets/play-anywhere.html', comment_play, '\n')
add_comment('app/src/main/assets/play-anywhere.html', comment_play, '\n')
