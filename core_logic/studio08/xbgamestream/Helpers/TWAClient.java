package com.studio08.xbgamestream.Helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.google.androidbrowserhelper.trusted.LauncherActivity;
/* loaded from: /app/base.apk/classes3.dex */
public class TWAClient {
    String config;
    Context mContext;

    public TWAClient(Context context, String str) {
        this.mContext = context;
        this.config = str;
        Log.e("TWACLIENT", "Started with config " + str);
    }

    public void launchTWSA(String str) {
        String str2;
        if (!str.contains("?")) {
            str2 = str + "?";
        } else {
            str2 = str + "&";
        }
        String str3 = str2 + "setConfig=" + this.config;
        Log.e("TWACLIENT", "Launched with url: " + str3);
        Intent intent = new Intent(this.mContext, LauncherActivity.class);
        intent.setData(Uri.parse(str3));
        this.mContext.startActivity(intent);
    }

    public boolean getShouldUseTWA() {
        return Helper.getRenderEngine(this.mContext).equals("chrome");
    }
}
