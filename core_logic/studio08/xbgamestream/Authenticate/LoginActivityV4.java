package com.studio08.xbgamestream.Authenticate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import com.studio08.xbgamestream.Authenticate.LoginClientV4;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public class LoginActivityV4 extends AppCompatActivity {
    private AlertDialog dialog;
    LoginClientV4 loginClient;
    LoginClientV4.LoginClientListener loginReadyListener = new LoginClientV4.LoginClientListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivityV4.2
        @Override // com.studio08.xbgamestream.Authenticate.LoginClientV4.LoginClientListener
        public void onLoginComplete() {
            try {
                if (LoginActivityV4.this.dialog != null && LoginActivityV4.this.dialog.isShowing()) {
                    LoginActivityV4.this.dialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            LoginActivityV4.this.closeActivity(true);
        }

        @Override // com.studio08.xbgamestream.Authenticate.LoginClientV4.LoginClientListener
        public void hideDialog() {
            try {
                if (LoginActivityV4.this.dialog != null) {
                    LoginActivityV4.this.dialog.hide();
                }
            } catch (Exception unused) {
            }
        }

        @Override // com.studio08.xbgamestream.Authenticate.LoginClientV4.LoginClientListener
        public void showDialog() {
            try {
                LoginActivityV4.this.dialog.show();
            } catch (Exception unused) {
            }
        }

        @Override // com.studio08.xbgamestream.Authenticate.LoginClientV4.LoginClientListener
        public void errorMessage(String str) {
            try {
                if (!LoginActivityV4.this.isFinishing() && !LoginActivityV4.this.isDestroyed()) {
                    new AlertDialog.Builder(LoginActivityV4.this).setTitle("Error").setMessage(str).setCancelable(false).setPositiveButton("Exit", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivityV4.2.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            LoginActivityV4.this.closeActivity(false);
                        }
                    }).show();
                } else {
                    Log.e("LoginActivityV4", "Activity is not valid to show the dialog");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private StreamWebview mainWebView;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login_v4);
        StreamWebview streamWebview = (StreamWebview) findViewById(R.id.webview1);
        this.mainWebView = streamWebview;
        streamWebview.init();
        this.mainWebView.setBackgroundColor(0);
        AlertDialog show = new AlertDialog.Builder(this).setMessage("Loading. Please wait...").setCancelable(false).setPositiveButton("Exit", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginActivityV4.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                LoginActivityV4.this.closeActivity(false);
            }
        }).setNegativeButton("Hide", (DialogInterface.OnClickListener) null).show();
        this.dialog = show;
        WindowManager.LayoutParams attributes = show.getWindow().getAttributes();
        attributes.dimAmount = 0.97f;
        this.dialog.getWindow().setAttributes(attributes);
        this.dialog.getWindow().addFlags(4);
        doLogin(this.mainWebView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closeActivity(boolean z) {
        Intent intent = new Intent();
        if (z) {
            setResult(-1, intent);
        } else {
            setResult(0, intent);
        }
        cleanUp();
        finish();
    }

    public void doLogin(StreamWebview streamWebview) {
        LoginClientV4 loginClientV4 = new LoginClientV4(getApplicationContext(), streamWebview);
        this.loginClient = loginClientV4;
        loginClientV4.setCustomObjectListener(this.loginReadyListener);
        this.loginClient.loginButtonClicked();
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
        } catch (Exception e) {
            e.printStackTrace();
            this.mainWebView = null;
        }
    }
}
