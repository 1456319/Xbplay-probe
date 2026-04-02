import pytest
from generate_mapping import is_stolen

def test_is_stolen_exact_matches():
    assert is_stolen("libffmpegkit_abidetect.so") is True
    assert is_stolen("old.html") is True
    assert is_stolen("warning-screen.html") is True
    assert is_stolen("warning.png") is True
    assert is_stolen("play-anywhere.html") is True
    assert is_stolen("xSDK client.js") is True

def test_is_stolen_substring_matches():
    assert is_stolen("assets/old.html") is True
    assert is_stolen("path/to/libffmpegkit_abidetect.so") is True
    assert is_stolen("some/folder/xSDK client.js") is True

def test_is_stolen_negative_cases():
    assert is_stolen("main.js") is False
    assert is_stolen("index.html") is False
    assert is_stolen("assets/icon.png") is False
    assert is_stolen("libffmpegkit_other.so") is False

def test_is_stolen_case_sensitivity():
    # Since 'in' is used on the original string, it should be case sensitive
    assert is_stolen("OLD.HTML") is False
    assert is_stolen("XSDK CLIENT.JS") is False
    assert is_stolen("Warning.png") is False
