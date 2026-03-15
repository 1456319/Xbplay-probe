package com.studio08.xbgamestream.ControllerSetup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class ControllerConfigActivity extends AppCompatActivity {
    String currentButton;
    private StreamWebview mainWebView;
    Map<Integer, String> overrideButtons = new HashMap();
    ApiClient.StreamingClientListener saveControllerListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.ControllerSetup.ControllerConfigActivity.1
        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void onCloseScreenDetected() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void onReLoginDetected() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void pressButtonWifiRemote(String str) {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void setOrientationValue(String str) {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void vibrate() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void genericMessage(String str, String str2) {
            if (str.equals("physical_controller_config_save")) {
                ControllerConfigActivity.this.saveControllerConfig(str2);
            }
            if (str.equals("physical_controller_config_current_button")) {
                ControllerConfigActivity.this.currentButton = str2;
            }
        }
    };
    ApiClient streamingClient;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_physical_controller_config);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Physical Controller Setup");
        StreamWebview streamWebview = (StreamWebview) findViewById(R.id.webview1);
        this.mainWebView = streamWebview;
        streamWebview.init();
        this.overrideButtons = new HashMap();
        doSetup();
    }

    public void doSetup() {
        ApiClient apiClient = new ApiClient(this, this.mainWebView);
        this.streamingClient = apiClient;
        apiClient.setCustomObjectListener(this.saveControllerListener);
        this.streamingClient.doPhysicalControllerSetup();
    }

    public void saveControllerConfig(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            SharedPreferences sharedPreferences = getSharedPreferences("SettingsSharedPref", 0);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(jSONObject.getString("name"), jSONObject.getJSONObject("data").toString());
            edit.apply();
            JSONArray jSONArray = new JSONArray(sharedPreferences.getString("physical_controller_configs", "[]"));
            jSONArray.put(jSONArray.length(), jSONObject);
            edit.putString("physical_controller_configs", jSONArray.toString());
            edit.apply();
            saveOverrides(jSONObject.getString("name"));
        } catch (Exception e) {
            Toast.makeText(this, "Error saving config" + e.getMessage(), 1).show();
        }
    }

    public void saveOverrides(String str) {
        if (this.overrideButtons.size() > 0) {
            SharedPreferences.Editor edit = getSharedPreferences("SettingsSharedPref", 0).edit();
            for (Map.Entry<Integer, String> entry : this.overrideButtons.entrySet()) {
                edit.putString("custom_input_" + str + "_" + entry.getKey(), entry.getValue());
                edit.apply();
            }
        }
    }

    @Override // androidx.appcompat.app.AppCompatActivity, android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        Log.e("HERE", "KEYPRESS: " + i + " SOURCE: " + keyEvent.getSource() + " Current Button:" + this.currentButton);
        if (!BuildConfig.FLAVOR.equals("tv")) {
            return super.onKeyDown(i, keyEvent);
        }
        if (TextUtils.isEmpty(this.currentButton) || (keyEvent.getSource() & 1025) != 1025) {
            return super.onKeyDown(i, keyEvent);
        }
        this.overrideButtons.put(Integer.valueOf(keyEvent.getKeyCode()), this.currentButton);
        this.streamingClient.sendSkipGamepadConfigButton();
        return false;
    }
}
