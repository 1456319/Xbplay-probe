package com.studio08.xbgamestream.Helpers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public class TutorialActivity extends AppCompatActivity {
    private StreamWebview mainWebView;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_tutorial);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setTitle("Tutorial");
        supportActionBar.hide();
        StreamWebview streamWebview = (StreamWebview) findViewById(R.id.webview1);
        this.mainWebView = streamWebview;
        streamWebview.init();
        this.mainWebView.setBackgroundColor(0);
        doTutorial(this.mainWebView);
    }

    public void doTutorial(StreamWebview streamWebview) {
        ApiClient apiClient = new ApiClient(this, streamWebview);
        apiClient.setCustomObjectListener(new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.Helpers.TutorialActivity.1
            @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
            public void genericMessage(String str, String str2) {
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
            public void onCloseScreenDetected() {
                new EncryptClient(TutorialActivity.this).saveValue("tutorialShown", "1");
                TutorialActivity.this.finish();
            }
        });
        apiClient.doTutorialScreens(getIntent().getBooleanExtra("show_full", false));
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("Exit").setMessage("Are you sure you want to exit tutorial").setCancelable(true).setPositiveButton("Close Tutorial", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.TutorialActivity.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                new EncryptClient(TutorialActivity.this).saveValue("tutorialShown", "1");
                Toast.makeText(TutorialActivity.this, "You can view the tutorial anytime in the settings", 1).show();
                TutorialActivity.this.finish();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.TutorialActivity.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }
}
