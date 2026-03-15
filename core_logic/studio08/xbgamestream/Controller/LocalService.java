package com.studio08.xbgamestream.Controller;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
/* loaded from: /app/base.apk/classes3.dex */
public class LocalService extends Service {
    VibrationEvent activeVibrationEvent;
    VibrationEvent lastSentVibEvent;
    Vibrator vb;
    boolean running = false;
    private final IBinder binder = new LocalBinder();

    /* loaded from: /app/base.apk/classes3.dex */
    static class VibrationEvent {
        long duration = 0;
        int amp = 0;

        VibrationEvent() {
        }
    }

    /* loaded from: /app/base.apk/classes3.dex */
    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public LocalService getService() {
            return LocalService.this;
        }
    }

    public void startRumbleLoop() {
        this.running = true;
        new Thread(new Runnable() { // from class: com.studio08.xbgamestream.Controller.LocalService.1
            @Override // java.lang.Runnable
            public void run() {
                while (LocalService.this.running) {
                    try {
                        if (LocalService.this.activeVibrationEvent != null) {
                            if (LocalService.this.activeVibrationEvent.amp > 0 && LocalService.this.activeVibrationEvent.duration > 0) {
                                if (Build.VERSION.SDK_INT >= 26) {
                                    int i = LocalService.this.activeVibrationEvent.amp;
                                    long j = LocalService.this.activeVibrationEvent.duration;
                                    if (i > 0 && j > 0) {
                                        Log.d("VIB", "Sending Vibration: " + i + " | " + LocalService.this.activeVibrationEvent.duration);
                                        LocalService.this.vb.vibrate(VibrationEffect.createOneShot(j, i), new AudioAttributes.Builder().setUsage(14).build());
                                        LocalService.this.activeVibrationEvent = null;
                                        Thread.sleep(200L);
                                    }
                                    return;
                                }
                                continue;
                            }
                            LocalService.this.vb.cancel();
                            LocalService.this.activeVibrationEvent = null;
                            Thread.sleep(100L);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override // android.app.Service
    public void onCreate() {
        Log.e("LocalService", "onCreate");
        this.vb = (Vibrator) getSystemService("vibrator");
        super.onCreate();
    }

    @Override // android.app.Service
    public void onRebind(Intent intent) {
        Log.e("LocalService", "onRebind");
        startRumbleLoop();
        super.onRebind(intent);
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        Log.e("LocalService", "onUnbind");
        this.running = false;
        return super.onUnbind(intent);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.e("LocalService", "onBind");
        startRumbleLoop();
        return this.binder;
    }

    public void sendPatter(long j, int i) {
        VibrationEvent vibrationEvent = new VibrationEvent();
        vibrationEvent.amp = i / 3;
        vibrationEvent.duration = j;
        this.activeVibrationEvent = vibrationEvent;
    }
}
