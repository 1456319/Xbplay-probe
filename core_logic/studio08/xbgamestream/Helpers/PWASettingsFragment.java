package com.studio08.xbgamestream.Helpers;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.widget.Toast;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import com.studio08.xbgamestream.ControllerSetup.ControllerConfigActivity;
import com.studio08.xbgamestream.R;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
/* loaded from: /app/base.apk/classes3.dex */
public class PWASettingsFragment extends PreferenceFragmentCompat {
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
        addPreferencesFromResource(R.xml.pwa_settings_prefs);
        try {
            findPreference("rate_app_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.1
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    PWASettingsFragment.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + PWASettingsFragment.this.getActivity().getPackageName())));
                    return true;
                }
            });
        } catch (Exception unused) {
        }
        try {
            findPreference("create_physical_controller_button_mappings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.2
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    if (!Helper.getRenderEngine(PWASettingsFragment.this.getContext()).equals("webview")) {
                        new AlertDialog.Builder(PWASettingsFragment.this.getActivity()).setTitle("Not Available").setMessage("You can't create custom controller mappings while using this render engine. If this is a feature you would like, please let me know in the discord channel or via email :)").setCancelable(true).setNegativeButton(HTTP.CONN_CLOSE, (DialogInterface.OnClickListener) null).show();
                    } else {
                        new AlertDialog.Builder(PWASettingsFragment.this.getActivity()).setTitle("Custom Controller Button Mappings").setMessage("This will begin the process to setup custom button mappings for a physical controller (connected to your device). Note, you should only have to do this for unique controllers, such as the Nintendo Switch.").setCancelable(true).setPositiveButton("Configure Now", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.2.1
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PWASettingsFragment.this.startCustomButtonMap();
                            }
                        }).setNegativeButton("Exit", (DialogInterface.OnClickListener) null).show();
                    }
                    return true;
                }
            });
        } catch (Exception unused2) {
        }
        try {
            findPreference("join_discord_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.3
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    PWASettingsFragment.this.joinDiscordServer();
                    return true;
                }
            });
        } catch (Exception unused3) {
        }
        try {
            findPreference("join_reddit_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.4
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    PWASettingsFragment.this.joinSubReddit();
                    return true;
                }
            });
        } catch (Exception unused4) {
        }
        try {
            findPreference("use_notch_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.5
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(PWASettingsFragment.this.getContext(), "Restart app to see changes.", 1).show();
                    return true;
                }
            });
        } catch (Exception unused5) {
        }
        findPreference("clear_cache_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.6
            @Override // androidx.preference.Preference.OnPreferenceClickListener
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(PWASettingsFragment.this.getActivity()).setTitle("Are you sure?").setMessage("This will delete all cached data").setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.6.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PWASettingsFragment.this.clearCache();
                    }
                }).setNegativeButton("Cancel", (DialogInterface.OnClickListener) null).show();
                return true;
            }
        });
        try {
            findPreference("pwa_use_legacy_theme_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.7
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(PWASettingsFragment.this.getActivity()).setTitle("Activating Legacy Theme").setMessage("You can revert back to the new theme at any time in the settings. Restart app to apply changes!").setCancelable(false).setPositiveButton("I Understand", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.7.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            PWASettingsFragment.this.clearCache();
                        }
                    }).show();
                    return true;
                }
            });
        } catch (Exception unused6) {
        }
        try {
            findPreference("use_audio_low_latency_mode_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.8
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(PWASettingsFragment.this.getActivity()).setTitle("Restart App").setMessage("You must restart the app to apply changes!").setCancelable(false).setPositiveButton("I Understand", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.8.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                    return true;
                }
            });
        } catch (Exception unused7) {
        }
        try {
            findPreference("use_wifi_low_latency_mode_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.9
                @Override // androidx.preference.Preference.OnPreferenceClickListener
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(PWASettingsFragment.this.getActivity()).setTitle("Restart App").setMessage("You must restart the app to apply changes!").setCancelable(false).setPositiveButton("I Understand", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.PWASettingsFragment.9.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                    return true;
                }
            });
        } catch (Exception unused8) {
        }
        populatePhysicalControllerMappings();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearCache() {
        Toast.makeText(getActivity(), "Cache Cleared", 1).show();
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        new EncryptClient(getContext()).deleteAll();
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
}
