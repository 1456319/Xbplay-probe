package com.studio08.xbgamestream.Web;
/* loaded from: /app/base.apk/classes3.dex */
public interface StreamWebviewListener {
    void closeScreen();

    void genericMessage(String str, String str2);

    void onReLoginRequest();

    void pressButtonWifiRemote(String str);

    void setOrientationValue(String str);

    void vibrate();
}
