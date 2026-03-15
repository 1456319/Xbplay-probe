package com.studio08.xbgamestream.Helpers;

import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;
/* loaded from: /app/base.apk/classes3.dex */
public class FirebaseAnalyticsClient {
    private FirebaseAnalytics mFirebaseAnalytics;

    public FirebaseAnalyticsClient(FirebaseAnalytics firebaseAnalytics) {
        this.mFirebaseAnalytics = firebaseAnalytics;
    }

    public void logButtonClickEvent(String str) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("item_id", str);
            this.mFirebaseAnalytics.logEvent(str + "_button_click", bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logFragmentCreated(String str) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("item_id", str);
            this.mFirebaseAnalytics.logEvent(str + "_fragment_loaded", bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logCustomEvent(String str, String str2) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("item_id", str2);
            this.mFirebaseAnalytics.logEvent(str + "_custom_event", bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
