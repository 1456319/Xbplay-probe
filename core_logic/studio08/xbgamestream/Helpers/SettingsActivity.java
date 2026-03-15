package com.studio08.xbgamestream.Helpers;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.studio08.xbgamestream.R;
/* loaded from: /app/base.apk/classes3.dex */
public class SettingsActivity extends AppCompatActivity {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);
        if (findViewById(R.id.idFrameLayout) == null || bundle != null) {
            return;
        }
        getSupportFragmentManager().beginTransaction().add(R.id.idFrameLayout, new SettingsFragment()).commit();
    }
}
