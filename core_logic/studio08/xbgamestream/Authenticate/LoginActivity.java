package com.studio08.xbgamestream.Authenticate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.studio08.xbgamestream.Authenticate.LoginClientV3;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.tapjoy.TJAdUnitConstants;
import org.apache.http.util.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class LoginActivity extends AppCompatActivity {
    private String consoleName;
    private AlertDialog dialog;
    LoginClientV3 loginClient;
    private boolean loginComplete = false;
    LoginClientV3.LoginClientListener loginReadyListener = new AnonymousClass2();
    private StreamWebview mainWebView;
    private String serverId;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        if (BuildConfig.FLAVOR.equals("tv")) {
            toolbar.setVisibility(8);
        }
        StreamWebview streamWebview = (StreamWebview) findViewById(R.id.webview1);
        this.mainWebView = streamWebview;
        streamWebview.init();
        this.mainWebView.setBackgroundColor(0);
        AlertDialog show = new AlertDialog.Builder(this).setTitle("Please Wait...").setMessage("Authenticating with Xbox Live").setCancelable(false).setPositiveButton("Exit", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                LoginActivity.this.closeActivity(false);
            }
        }).setNegativeButton("Hide", (DialogInterface.OnClickListener) null).show();
        this.dialog = show;
        WindowManager.LayoutParams attributes = show.getWindow().getAttributes();
        attributes.dimAmount = 0.97f;
        this.dialog.getWindow().setAttributes(attributes);
        this.dialog.getWindow().addFlags(4);
        doLogin(this.mainWebView);
    }

    /* renamed from: com.studio08.xbgamestream.Authenticate.LoginActivity$2  reason: invalid class name */
    /* loaded from: /app/base.apk/classes3.dex */
    class AnonymousClass2 implements LoginClientV3.LoginClientListener {
        AnonymousClass2() {
        }

        @Override // com.studio08.xbgamestream.Authenticate.LoginClientV3.LoginClientListener
        public void onLoginComplete(String str) {
            try {
                LoginActivity.this.loginComplete = true;
                if (LoginActivity.this.dialog != null && LoginActivity.this.dialog.isShowing()) {
                    LoginActivity.this.dialog.dismiss();
                }
                LoginActivity.this.promptForConsole(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override // com.studio08.xbgamestream.Authenticate.LoginClientV3.LoginClientListener
        public void statusMessage(final String str) {
            Log.e("StatusMessage", str);
            LoginActivity.this.runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.2.1
                @Override // java.lang.Runnable
                public void run() {
                    AnonymousClass2.this.showDialog();
                    LoginActivity.this.dialog.setMessage(str);
                }
            });
        }

        @Override // com.studio08.xbgamestream.Authenticate.LoginClientV3.LoginClientListener
        public void hideDialog() {
            try {
                if (LoginActivity.this.dialog != null) {
                    LoginActivity.this.dialog.hide();
                }
            } catch (Exception unused) {
            }
        }

        @Override // com.studio08.xbgamestream.Authenticate.LoginClientV3.LoginClientListener
        public void showDialog() {
            try {
                if (!LoginActivity.this.loginComplete) {
                    LoginActivity.this.dialog.show();
                } else {
                    Log.e("HRE", "Ignoring show dialog because already got valid response");
                }
            } catch (Exception unused) {
            }
        }

        @Override // com.studio08.xbgamestream.Authenticate.LoginClientV3.LoginClientListener
        public void genericMessage(String str, String str2) {
            if (str.equals("cant_login") && str2.equals("InvalidCountry")) {
                Toast.makeText(LoginActivity.this, "Possibly Invalid Location", 1).show();
                new AlertDialog.Builder(LoginActivity.this).setTitle("Invalid Location").setMessage("You appear to be in a location that doesn't support XCloud. That's OK! Click the 'exit' button and try again. It should work the second time. If you still see this error. Please report it!").setCancelable(false).setPositiveButton("Exit", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.2.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LoginActivity.this.closeActivity(false);
                    }
                }).setNegativeButton("Hide", (DialogInterface.OnClickListener) null).show();
            }
        }
    }

    public void promptForConsole(String str) {
        try {
            JSONArray jSONArray = new JSONObject(str).getJSONArray("results");
            final String[] strArr = new String[jSONArray.length()];
            final String[] strArr2 = new String[jSONArray.length()];
            for (int i = 0; i < jSONArray.length(); i++) {
                String string = jSONArray.getJSONObject(i).getString("deviceName");
                String string2 = jSONArray.getJSONObject(i).getString("serverId");
                strArr[i] = string + " - " + string2;
                strArr2[i] = string2;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            if (jSONArray.length() < 1) {
                final SharedPreferences sharedPreferences = getSharedPreferences("SettingsSharedPref", 0);
                if (sharedPreferences.getBoolean("ignore_no_consoles_warning", false)) {
                    Toast.makeText(this, "Warning: No consoles found.", 1).show();
                    closeActivity(false);
                    return;
                }
                builder.setTitle("Warning: No Consoles Found");
                builder.setMessage("We couldn't find any Xbox consoles associated with your account. Ensure 'Remote Features' is enabled in your console's 'Settings->Devices & Connections' page for the same profile you signed in for in this app. Additionally, make sure you are connected to the same WiFi network as your console.\n\nTo login to a different account, click 'clear cache' in the settings of this app.\n\nIf you only intend to use xCloud, ignore this warning.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        LoginActivity.this.closeActivity(true);
                    }
                });
                builder.setNegativeButton("Don't Show Again", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.4
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        SharedPreferences.Editor edit = sharedPreferences.edit();
                        edit.putBoolean("ignore_no_consoles_warning", true);
                        edit.apply();
                        LoginActivity.this.closeActivity(false);
                    }
                });
                builder.setNeutralButton("Help", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.5
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        LoginActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://support.xbox.com/en-US/help/games-apps/game-setup-and-play/how-to-set-up-remote-play")));
                        LoginActivity.this.closeActivity(false);
                    }
                });
            } else {
                final EncryptClient encryptClient = new EncryptClient(this);
                if (!TextUtils.isEmpty(encryptClient.getValue("rememberConsole")) && !TextUtils.isEmpty(encryptClient.getValue("serverId"))) {
                    this.serverId = encryptClient.getValue("serverId");
                    closeActivity(true);
                    return;
                }
                builder.setTitle("Choose a default console");
                this.serverId = strArr2[0];
                this.consoleName = strArr[0];
                builder.setSingleChoiceItems(strArr, 0, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.6
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        LoginActivity.this.serverId = strArr2[i2];
                        LoginActivity.this.consoleName = strArr[i2];
                    }
                });
                builder.setNeutralButton(TJAdUnitConstants.SHARE_CHOOSE_TITLE, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.7
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        encryptClient.saveValue("serverId", LoginActivity.this.serverId);
                        LoginActivity.this.closeActivity(true);
                    }
                });
                builder.setPositiveButton("Remember", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.8
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        encryptClient.saveValue("serverId", LoginActivity.this.serverId);
                        encryptClient.saveValue("rememberConsole", LoginActivity.this.serverId);
                        Toast.makeText(LoginActivity.this, "Use 'Settings > Forget Saved Console' to use a new console.", 1).show();
                        LoginActivity.this.closeActivity(true);
                    }
                });
                builder.setNegativeButton("Back", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivity.9
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        Toast.makeText(LoginActivity.this, "Cant connect to console. User closed", 1).show();
                        LoginActivity.this.closeActivity(false);
                    }
                });
            }
            try {
                builder.create().show();
            } catch (Exception unused) {
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closeActivity(boolean z) {
        Intent intent = new Intent();
        if (z) {
            intent.putExtra("serverId", this.serverId);
            intent.putExtra("consoleName", this.consoleName);
            setResult(-1, intent);
        } else {
            setResult(0, intent);
        }
        cleanUp();
        finish();
    }

    public void doLogin(StreamWebview streamWebview) {
        LoginClientV3 loginClientV3 = new LoginClientV3(this, streamWebview);
        this.loginClient = loginClientV3;
        loginClientV3.setCustomObjectListener(this.loginReadyListener);
        this.loginClient.doLogin();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    public void cleanUp() {
        try {
            StreamWebview streamWebview = this.mainWebView;
            if (streamWebview != null) {
                ((ViewGroup) streamWebview.getParent()).removeAllViews();
                this.mainWebView.clearHistory();
                this.mainWebView.clearCache(false);
                this.mainWebView.loadUrl("about:blank");
                this.mainWebView.onPause();
                this.mainWebView.removeAllViews();
                this.mainWebView.destroy();
                this.mainWebView = null;
            }
            LoginClientV3 loginClientV3 = this.loginClient;
            if (loginClientV3 != null) {
                loginClientV3.pollerRunning = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.mainWebView = null;
        }
    }
}
