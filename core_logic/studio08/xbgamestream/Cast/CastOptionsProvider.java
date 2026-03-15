package com.studio08.xbgamestream.Cast;

import android.content.Context;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;
import com.studio08.xbgamestream.Web.ApiClient;
import java.util.ArrayList;
import java.util.List;
/* loaded from: /app/base.apk/classes3.dex */
public class CastOptionsProvider implements OptionsProvider {
    @Override // com.google.android.gms.cast.framework.OptionsProvider
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }

    @Override // com.google.android.gms.cast.framework.OptionsProvider
    public CastOptions getCastOptions(Context context) {
        String str;
        ArrayList arrayList = new ArrayList();
        arrayList.add(MediaIntentReceiver.ACTION_STOP_CASTING);
        CastMediaOptions build = new CastMediaOptions.Builder().setNotificationOptions(new NotificationOptions.Builder().setActions(arrayList, new int[]{0}).setTargetActivityClassName(MyExpandedControls.class.getName()).build()).build();
        if ("release".equals("debug") && ApiClient.USE_DEV) {
            str = "30DD2B6B";
        } else {
            str = "3E940AFA";
        }
        return new CastOptions.Builder().setCastMediaOptions(build).setReceiverApplicationId(str).build();
    }
}
