import os
import pytest
from unittest.mock import patch, mock_open, MagicMock
from stolen_assets_script import generate_report

def test_generate_report_success():
    """Test successful report generation with mock files."""
    mock_files = ["test.html", "test.png"]
    asset_dir = "mock_assets"

    # Mocking os.listdir to return our mock files
    with patch("os.listdir", return_value=mock_files):
        # Mocking open to handle both reading assets and writing the report
        def side_effect(filename, mode="r"):
            if "test.html" in filename:
                if "b" in mode:
                    return mock_open(read_data=b"<html>STREAM_VIEW_URL gssv-play-prodxhome.xboxlive.com xSDK client.js</html>").return_value
                return mock_open(read_data="<html>STREAM_VIEW_URL gssv-play-prodxhome.xboxlive.com xSDK client.js</html>").return_value
            elif "test.png" in filename:
                return mock_open(read_data=b"fake png data").return_value
            elif "stolen_asset_report.md" in filename:
                return mock_open().return_value
            return mock_open().return_value

        with patch("builtins.open", side_effect=side_effect) as mocked_open:
            with patch("hashlib.sha256") as mocked_hash:
                mocked_hash.return_value.hexdigest.return_value = "fake_hash"
                generate_report(asset_dir)

                mocked_open.assert_any_call("stolen_asset_report.md", "w")

def test_generate_report_dir_not_found():
    """Test FileNotFoundError when the asset directory does not exist."""
    asset_dir = "non_existent_dir"
    with patch("os.listdir", side_effect=FileNotFoundError):
        with patch("builtins.print") as mocked_print:
            generate_report(asset_dir)
            mocked_print.assert_called_with(f"Error: The directory '{asset_dir}' does not exist.")

def test_generate_report_permission_error():
    """Test PermissionError when accessing the directory."""
    asset_dir = "restricted_dir"
    with patch("os.listdir", side_effect=PermissionError):
        with patch("builtins.print") as mocked_print:
            generate_report(asset_dir)
            mocked_print.assert_called_with(f"Error: Permission denied when accessing '{asset_dir}'.")

def test_generate_report_empty_dir():
    """Test empty asset directory."""
    asset_dir = "empty_dir"
    with patch("os.listdir", return_value=[]):
        with patch("builtins.open", mock_open()) as mocked_open:
            generate_report(asset_dir)
            mocked_open.assert_called_with("stolen_asset_report.md", "w")

def test_generate_report_file_error_continues():
    """Test that an error processing one file doesn't stop the whole report."""
    asset_dir = "mock_assets"
    mock_files = ["good.html", "bad.html"]

    with patch("os.listdir", return_value=mock_files):
        def side_effect(filename, mode="r"):
            if "good.html" in filename:
                if "b" in mode:
                    return mock_open(read_data=b"good").return_value
                return mock_open(read_data="good").return_value
            elif "bad.html" in filename:
                raise OSError("Simulated file error")
            return mock_open().return_value

        with patch("builtins.open", side_effect=side_effect):
            with patch("builtins.print") as mocked_print:
                # We still need to mock hashlib.sha256 if we don't want to worry about bytes vs strings in the mock
                # OR we ensure the mock returns bytes. I'll do both for robustness.
                with patch("hashlib.sha256") as mocked_hash:
                    mocked_hash.return_value.hexdigest.return_value = "fake_hash"
                    generate_report(asset_dir)
                    mocked_print.assert_any_call("Warning: Could not process file 'bad.html': Simulated file error")
                    mocked_print.assert_any_call("Report generated.")
