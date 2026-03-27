from generate_mapping import is_stolen, STOLEN_ASSETS
import pytest

def test_is_stolen_exact_match():
    """Test that all assets in STOLEN_ASSETS are correctly identified."""
    for asset in STOLEN_ASSETS:
        assert is_stolen(asset) is True

def test_is_stolen_path_match():
    """Test that paths containing stolen assets are correctly identified."""
    assert is_stolen("assets/old.html") is True
    assert is_stolen("/usr/lib/libffmpegkit_abidetect.so") is True
    assert is_stolen("some/random/path/xSDK client.js") is True

def test_is_stolen_no_match():
    """Test that non-stolen assets return False."""
    assert is_stolen("index.html") is False
    assert is_stolen("README.md") is False
    assert is_stolen("libffmpegkit.so") is False

def test_is_stolen_empty_string():
    """Test that an empty string returns False."""
    assert is_stolen("") is False

def test_is_stolen_case_sensitivity():
    """Test that the check is case-sensitive, reflecting current implementation."""
    # current implementation uses 'in' without lower() on the pattern
    assert is_stolen("OLD.HTML") is False
    assert is_stolen("old.html") is True
