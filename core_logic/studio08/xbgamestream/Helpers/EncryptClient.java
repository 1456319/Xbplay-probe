package com.studio08.xbgamestream.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class EncryptClient {
    private Context context;
    private String masterKeyAlias = null;
    private SharedPreferences sharedPreference = null;

    public EncryptClient(Context context) {
        this.context = context;
        init();
    }

    public String getValue(String str) {
        SharedPreferences sharedPreferences = this.sharedPreference;
        return sharedPreferences == null ? "" : sharedPreferences.getString(str, "");
    }

    public void saveValue(String str, String str2) {
        this.sharedPreference.edit().putString(str, str2).apply();
    }

    public void deleteValue(String str) {
        this.sharedPreference.edit().remove(str).apply();
    }

    public void deleteAll() {
        SharedPreferences sharedPreferences = this.sharedPreference;
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().commit();
        }
    }

    public void saveJSONObject(String str, JSONObject jSONObject) {
        try {
            saveValue(str, jSONObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSONObject(String str) {
        String value = getValue(str);
        if (value != null && !value.isEmpty()) {
            try {
                return new JSONObject(value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean init() {
        try {
            String orCreate = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            this.masterKeyAlias = orCreate;
            this.sharedPreference = EncryptedSharedPreferences.create("secret_shared_prefs", orCreate, this.context, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }
}
