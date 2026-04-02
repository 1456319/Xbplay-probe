from generate_mapping import is_stolen

def test_is_stolen_exact_match():
    assert is_stolen("old.html") is True
    assert is_stolen("libffmpegkit_abidetect.so") is True
    assert is_stolen("xSDK client.js") is True

def test_is_stolen_substring_match():
    assert is_stolen("assets/old.html") is True
    assert is_stolen("path/to/libffmpegkit_abidetect.so") is True
    assert is_stolen("extracted_assets/xSDK client.js") is True

def test_is_stolen_negative_match():
    assert is_stolen("main.js") is False
    assert is_stolen("index.html") is False
    assert is_stolen("utils/helper.py") is False

def test_is_stolen_case_sensitivity():
    # is_stolen currently uses `if asset in path:`, which is case sensitive in Python strings.
    assert is_stolen("OLD.HTML") is False
    assert is_stolen("XSDK CLIENT.JS") is False
