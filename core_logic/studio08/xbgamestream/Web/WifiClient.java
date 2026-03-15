package com.studio08.xbgamestream.Web;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import com.tapjoy.TapjoyConstants;
/* loaded from: /app/base.apk/classes3.dex */
public class WifiClient {
    private WifiManager.WifiLock wifiLock;
    private final WifiManager wifiManager;

    public WifiClient(Context context) {
        this.wifiManager = (WifiManager) context.getSystemService(TapjoyConstants.TJC_CONNECTION_TYPE_WIFI);
    }

    public void acquireWifiLock() {
        new Thread(new Runnable() { // from class: com.studio08.xbgamestream.Web.WifiClient.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (WifiClient.this.wifiLock == null) {
                        if (Build.VERSION.SDK_INT >= 29) {
                            WifiClient wifiClient = WifiClient.this;
                            wifiClient.wifiLock = wifiClient.wifiManager.createWifiLock(4, "WebRTC_LowLatencyLock");
                        } else {
                            WifiClient wifiClient2 = WifiClient.this;
                            wifiClient2.wifiLock = wifiClient2.wifiManager.createWifiLock(3, "WebRTC_LowLatencyLock");
                        }
                    }
                    if (WifiClient.this.wifiLock.isHeld()) {
                        return;
                    }
                    WifiClient.this.wifiLock.acquire();
                    Log.w("WifiClient", "Wi-Fi Low Latency Lock Acquired");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void acquireWifiLockOld() {
        try {
            if (this.wifiLock == null) {
                if (Build.VERSION.SDK_INT >= 29) {
                    this.wifiLock = this.wifiManager.createWifiLock(4, "WebRTC_LowLatencyLock");
                } else {
                    this.wifiLock = this.wifiManager.createWifiLock(3, "WebRTC_LowLatencyLock");
                }
            }
            if (this.wifiLock.isHeld()) {
                return;
            }
            this.wifiLock.acquire();
            Log.w("WifiClient", "Wi-Fi Low Latency Lock Acquired");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseWifiLock() {
        try {
            WifiManager.WifiLock wifiLock = this.wifiLock;
            if (wifiLock == null || !wifiLock.isHeld()) {
                return;
            }
            this.wifiLock.release();
            Log.w("WifiClient", "Wi-Fi Low Latency Lock Released");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
