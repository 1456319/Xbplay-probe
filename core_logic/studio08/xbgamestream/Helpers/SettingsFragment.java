package com.studio08.xbgamestream.Helpers;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.widget.Toast;
import androidx.core.net.MailTo;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;
import com.studio08.xbgamestream.ControllerSetup.ControllerConfigActivity;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Timers.PurchaseChecker;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
/* loaded from: /app/base.apk/classes3.dex */
public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String PREF_FILE_NAME = "SettingsSharedPref";

    @Override // androidx.preference.PreferenceFragmentCompat
    public void onCreatePreferences(Bundle bundle, String str) {
    }

    @Override // androidx.preference.PreferenceFragmentCompat
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        super.setPreferenceScreen(preferenceScreen);
        if (preferenceScreen != null) {
            int preferenceCount = preferenceScreen.getPreferenceCount();
            for (int i = 0; i < preferenceCount; i++) {
                preferenceScreen.getPreference(i).setIconSpaceReserved(false);
                if (preferenceScreen.getPreference(i) instanceof PreferenceGroup) {
                    int preferenceCount2 = ((PreferenceGroup) preferenceScreen.getPreference(i)).getPreferenceCount();
                    for (int i2 = 0; i2 < preferenceCount2; i2++) {
                        ((PreferenceGroup) preferenceScreen.getPreference(i)).getPreference(i2).setIconSpaceReserved(false);
                    }
                }
            }
        }
    }

    @Override // androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getPreferenceManager().setSharedPreferencesName("SettingsSharedPref");
        addPreferencesFromResource(R.xml.settings_prefs);
        final RewardedAdLoader rewardedAdLoader = new RewardedAdLoader(getActivity());
        findPreference("clear_cache_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.1
            @Override // androidx.preference.Preference.OnPreferenceClickListener
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(SettingsFragment.this.getActivity()).setTitle("Are you sure?").setMessage("This will delete all cached data including tokens and controllers").setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.1.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SettingsFragment.this.clearCache();
                    }
                }).setNegativeButton("Cancel", (DialogInterface.OnClickListener) null).show();
                return true;
            }
        });
        try {
            findPreference("show_tutorial_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.2
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsFragment.this.getActivity(), TutorialActivity.class);
                    intent.putExtra("show_full", true);
                    SettingsFragment.this.startActivity(intent);
                    return true;
                }
            });
        } catch (Exception unused) {
        }
        try {
            findPreference("rate_app_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.3
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    SettingsFragment.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + SettingsFragment.this.getActivity().getPackageName())));
                    return true;
                }
            });
        } catch (Exception unused2) {
        }
        try {
            findPreference("create_physical_controller_button_mappings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.4
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    if (!Helper.getRenderEngine(SettingsFragment.this.getContext()).equals("webview")) {
                        new AlertDialog.Builder(SettingsFragment.this.getActivity()).setTitle("Not Available").setMessage("You can't create custom controller mappings while using this render engine. If this is a feature you would like, please let me know in the discord channel or via email :)").setCancelable(true).setNegativeButton(HTTP.CONN_CLOSE, (DialogInterface.OnClickListener) null).show();
                    } else {
                        new AlertDialog.Builder(SettingsFragment.this.getActivity()).setTitle("Custom Controller Button Mappings").setMessage("This will begin the process to setup custom button mappings for a physical controller (connected to your device). Note, you should only have to do this for unique controllers, such as the Nintendo Switch.").setCancelable(true).setPositiveButton("Configure Now", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.4.1
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SettingsFragment.this.startCustomButtonMap();
                            }
                        }).setNegativeButton("Exit", (DialogInterface.OnClickListener) null).show();
                    }
                    return true;
                }
            });
        } catch (Exception unused3) {
        }
        try {
            findPreference("emulate_client_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.5
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(SettingsFragment.this.getActivity()).setTitle("Info").setMessage("Changing between clients might require you to restart your console and re-login to your account. If you notice that the quality doesn't change or that you can't stream 360 games, please restart your console and re-sign into your account.").setCancelable(true).setPositiveButton("I understand", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.5.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).show();
                    return true;
                }
            });
        } catch (Exception unused4) {
        }
        try {
            findPreference("mini_gamepad_size_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.6
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(SettingsFragment.this.getActivity()).setTitle("Info").setMessage("Since you are changing the size of the Mini Gamepad, any custom controller layouts that you created for the Mini Gamepoad via the 'Gamepad Builder' feature might also need to be resized. If you notice that that your custom layout no longer scales properly, open the 'Gamepad Builder' tab and rebuild the layout (for the larger Mini Gamepad size)").setCancelable(true).setPositiveButton("I understand", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.6.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).show();
                    return true;
                }
            });
        } catch (Exception unused5) {
        }
        try {
            findPreference("join_discord_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.7
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    SettingsFragment.this.joinDiscordServer();
                    return true;
                }
            });
        } catch (Exception unused6) {
        }
        try {
            findPreference("join_reddit_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.8
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    SettingsFragment.this.joinSubReddit();
                    return true;
                }
            });
        } catch (Exception unused7) {
        }
        try {
            findPreference("region_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.9
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(SettingsFragment.this.getActivity()).setTitle("Info").setMessage("You must open the consoles tab again to use a new region.").setCancelable(true).setPositiveButton("I understand", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.9.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).show();
                    return true;
                }
            });
        } catch (Exception unused8) {
        }
        try {
            findPreference("use_notch_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.10
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(SettingsFragment.this.getContext(), "Restart app to see changes.", 1).show();
                    return true;
                }
            });
        } catch (Exception unused9) {
        }
        try {
            findPreference("ask_for_help_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.11
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    SettingsFragment.this.sendSupportEmail();
                    return true;
                }
            });
        } catch (Exception unused10) {
        }
        try {
            findPreference("forget_saved_console_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.12
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new EncryptClient(SettingsFragment.this.getContext()).saveValue("rememberConsole", "");
                    Toast.makeText(SettingsFragment.this.getActivity(), "Console cleared. Re-login and select a new default console", 1).show();
                    return true;
                }
            });
        } catch (Exception unused11) {
        }
        try {
            findPreference("custom_local_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.13
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(SettingsFragment.this.getActivity()).setTitle("Info").setMessage("If you already started a game with a different language within the past 5 minutes, you must restart the game for the new language to be applied. Restart the game by starting any other xCloud game.").setCancelable(true).setPositiveButton("I understand", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.13.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).show();
                    return true;
                }
            });
        } catch (Exception unused12) {
        }
        try {
            findPreference("orientation_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.14
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(SettingsFragment.this.getContext(), "Restart app to see changes.", 1).show();
                    return true;
                }
            });
        } catch (Exception unused13) {
        }
        try {
            findPreference("unlock_full_version_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.15
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    rewardedAdLoader.buyAdRemoval();
                    return true;
                }
            });
        } catch (Exception unused14) {
        }
        try {
            findPreference("unlock_full_version_vr_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.16
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new PurchaseChecker(SettingsFragment.this.getContext(), null).showLicenseCheckDialog(true);
                    return true;
                }
            });
        } catch (Exception unused15) {
        }
        try {
            findPreference("unlock_full_version_main_app_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.17
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    rewardedAdLoader.buyAdRemoval();
                    return true;
                }
            });
        } catch (Exception unused16) {
        }
        try {
            findPreference("restore_purchase_main_app_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.18
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences.Editor edit = SettingsFragment.this.getContext().getSharedPreferences("SettingsSharedPref", 0).edit();
                    edit.putLong("nextMakeGetTokenRequest", 0L);
                    edit.apply();
                    EncryptClient encryptClient = new EncryptClient(SettingsFragment.this.getContext());
                    encryptClient.saveValue("purchaseToken", SessionDescription.SUPPORTED_SDP_VERSION);
                    rewardedAdLoader.queryPurchases();
                    if (TextUtils.isEmpty(encryptClient.getValue("gsToken"))) {
                        Toast.makeText(SettingsFragment.this.getActivity(), "Not logged in. You must be logged in for cross restore to work.", 1).show();
                    }
                    return true;
                }
            });
        } catch (Exception unused17) {
        }
        try {
            findPreference("pwa_use_legacy_theme_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.19
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(SettingsFragment.this.getActivity()).setTitle("Change Theme").setMessage("Restart app to apply changes.").setCancelable(true).setPositiveButton("Close App", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.SettingsFragment.19.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SettingsFragment.this.clearCache();
                            dialogInterface.dismiss();
                            SettingsFragment.this.getActivity().finishAffinity();
                        }
                    }).setNegativeButton("Cancel", (DialogInterface.OnClickListener) null).show();
                    return true;
                }
            });
        } catch (Exception unused18) {
        }
        populatePhysicalControllerMappings();
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        populatePhysicalControllerMappings();
    }

    private void populatePhysicalControllerMappings() {
        int i = 0;
        try {
            JSONArray jSONArray = new JSONArray(getActivity().getSharedPreferences("SettingsSharedPref", 0).getString("physical_controller_configs", "[]"));
            int length = jSONArray.length();
            int i2 = length + 1;
            CharSequence[] charSequenceArr = new CharSequence[i2];
            CharSequence[] charSequenceArr2 = new CharSequence[i2];
            charSequenceArr[0] = CookieSpecs.DEFAULT;
            charSequenceArr2[0] = "null";
            while (i < length) {
                String string = jSONArray.getJSONObject(i).getString("name");
                i++;
                charSequenceArr[i] = string;
                charSequenceArr2[i] = string;
            }
            ListPreference listPreference = (ListPreference) findPreference("physical_controller_button_mappings");
            listPreference.setEntries(charSequenceArr);
            listPreference.setEntryValues(charSequenceArr2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendSupportEmail() {
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.setData(Uri.parse(MailTo.MAILTO_SCHEME));
        intent.putExtra("android.intent.extra.EMAIL", new String[]{"alexwarddev1230@gmail.com"});
        intent.putExtra("android.intent.extra.SUBJECT", "xbPlay Support");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SettingsSharedPref", 0);
        boolean z = sharedPreferences.getBoolean("capture_debug_logs_key", false);
        String str = sharedPreferences.getBoolean("capture_debug_logs_gameplay_key", false) ? "Please type a detailed description of the issue you are facing below:\n\n\nGamePlay Logs: " + sharedPreferences.getString("gameplay_logs", "empty") : "Please type a detailed description of the issue you are facing below:\n";
        if (z) {
            str = str + "\n\nLogin Logs: " + sharedPreferences.getString("login_logs", "empty");
        }
        intent.putExtra("android.intent.extra.TEXT", str);
        try {
            startActivity(Intent.createChooser(intent, "Send support mail..."));
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(getActivity(), "There are no email clients installed.", 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void joinDiscordServer() {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://discord.gg/zxEBXxWWza")));
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(getActivity(), "Error opening discord link", 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void joinSubReddit() {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://www.reddit.com/r/xbPlay/")));
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(getActivity(), "Error opening reddit link", 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startCustomButtonMap() {
        startActivity(new Intent(getActivity(), ControllerConfigActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearCache() {
        Toast.makeText(getActivity(), "Clearing cache", 1).show();
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        EncryptClient encryptClient = new EncryptClient(getContext());
        encryptClient.saveValue("serverId", "");
        encryptClient.saveValue("gsToken", "");
        encryptClient.saveValue("streamCookieRaw", "");
        encryptClient.saveValue("xcloudToken", "");
        encryptClient.saveValue("xcloudRegion", "");
        encryptClient.saveValue("msalAccessToken", "");
        encryptClient.saveValue("msalRefreshToken", "");
        encryptClient.saveValue("clientId", "");
        encryptClient.saveValue("consoles", "");
        encryptClient.saveValue("purchaseToken", SessionDescription.SUPPORTED_SDP_VERSION);
        try {
            SharedPreferences.Editor edit = getContext().getSharedPreferences("SettingsSharedPref", 0).edit();
            edit.putLong("nextMakeGetTokenRequest", 0L);
            edit.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
