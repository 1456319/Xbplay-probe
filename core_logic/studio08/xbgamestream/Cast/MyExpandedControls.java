package com.studio08.xbgamestream.Cast;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;
import com.studio08.xbgamestream.MainActivity;
/* loaded from: /app/base.apk/classes3.dex */
public class MyExpandedControls extends ExpandedControllerActivity {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("showCastRemote", true);
        startActivity(intent);
        finish();
    }
}
