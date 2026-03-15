package com.studio08.xbgamestream.Widgets;

import Interfaces.SmartglassEvents;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.R;
import network.BindService;
/* loaded from: /app/base.apk/classes3.dex */
public class PowerWidgetProvider extends AppWidgetProvider implements SmartglassEvents {
    public BindService mBoundService;
    public Context mContext;
    public Boolean mServiceBound = false;
    private ServiceConnection localServiceConnection = new ServiceConnection() { // from class: com.studio08.xbgamestream.Widgets.PowerWidgetProvider.1
        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            PowerWidgetProvider.this.mServiceBound = false;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PowerWidgetProvider.this.mBoundService = ((BindService.MyBinder) iBinder).getService();
            PowerWidgetProvider.this.mServiceBound = true;
            PowerWidgetProvider.this.mBoundService.setListener(PowerWidgetProvider.this);
            PowerWidgetProvider.this.mBoundService.discover();
        }
    };

    @Override // android.appwidget.AppWidgetProvider
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] iArr) {
        Log.e("here", "ON UPDATE" + iArr.length);
        updateWidgetViews(context, false);
    }

    void updateWidgetViews(Context context, boolean z) {
        Log.e("Here", "Called updateWidgetViews: " + z);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_power);
        ComponentName componentName = new ComponentName(context, PowerWidgetProvider.class);
        if (z) {
            remoteViews.setViewVisibility(R.id.power_on, 4);
            remoteViews.setViewVisibility(R.id.power_waiting, 0);
        } else {
            remoteViews.setOnClickPendingIntent(R.id.power_on, getPendingSelfIntent(context, "power"));
            remoteViews.setViewVisibility(R.id.power_on, 0);
            remoteViews.setViewVisibility(R.id.power_waiting, 4);
        }
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }

    PendingIntent getPendingSelfIntent(Context context, String str) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(str);
        return PendingIntent.getBroadcast(context, 0, intent, AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL);
    }

    public void sendXboxCommand(Intent intent, Context context) {
        if (intent.getAction() != null) {
            Log.e("here", "Receive value: " + intent.getAction());
            if (!TextUtils.isEmpty(intent.getAction()) && !intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS")) {
                ((Vibrator) context.getSystemService("vibrator")).vibrate(40L, new AudioAttributes.Builder().setContentType(4).setUsage(4).build());
                sendBroadcast(context, intent.getAction());
                return;
            }
            Log.e("here", "Receive BAD command: " + intent.getAction());
        }
    }

    @Override // android.appwidget.AppWidgetProvider, android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e("here", "Receive");
        this.mContext = context;
        if (TextUtils.isEmpty(intent.getAction()) || !intent.getAction().equals("power")) {
            return;
        }
        updateWidgetViews(context, true);
        retryConnect(3, context.getApplicationContext());
        sendXboxCommand(intent, context);
    }

    public void sendBroadcast(Context context, String str) {
        Intent intent = new Intent();
        intent.setAction("buttonPress");
        intent.putExtra("buttonValue", str);
        context.sendBroadcast(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void retryConnect(final int i, final Context context) {
        updateWidgetViews(context, true);
        if (i <= 0) {
            Log.e("HERE", "Error connecting, out of retries!!!!");
            Toast.makeText(context.getApplicationContext(), "Cannot connect to Xbox!", 0).show();
            updateWidgetViews(context, false);
            return;
        }
        if (TextUtils.isEmpty(getLiveId(context))) {
            Log.e("HERE", "Cant use widget until initial connect");
            Toast.makeText(context.getApplicationContext(), "Your Xbox must be powered on for first time setup", 0).show();
        }
        if (!bindToService(context)) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.Widgets.PowerWidgetProvider.2
                @Override // java.lang.Runnable
                public void run() {
                    if (PowerWidgetProvider.this.bindToService(context)) {
                        return;
                    }
                    PowerWidgetProvider.this.retryConnect(i - 1, context);
                }
            }, 3000L);
        } else {
            updateWidgetViews(context, false);
        }
    }

    public boolean checkIfConnected() {
        if (this.mServiceBound.booleanValue() && this.mBoundService.ready) {
            Log.e("HERE", "Service is running and connected!");
            return true;
        }
        Log.e("HERE", "Service is not connected!");
        return false;
    }

    public boolean bindToService(Context context) {
        if (checkIfConnected()) {
            Log.e("HERE", "Detected service already running! Not restarting.");
            updateWidgetViews(context, false);
            return true;
        }
        BindService bindService = this.mBoundService;
        if (bindService != null) {
            bindService.powerOn(getLiveId(context));
        }
        Intent intent = new Intent(context, BindService.class);
        context.getSharedPreferences("SettingsSharedPref", 0).getString("notification_remote_key", "close_on_exit");
        intent.putExtra("notification_type", "always_show");
        ContextCompat.startForegroundService(context.getApplicationContext(), intent);
        context.bindService(intent, this.localServiceConnection, 1);
        if (this.mServiceBound.booleanValue()) {
            this.mBoundService.discover();
        }
        return false;
    }

    @Override // Interfaces.SmartglassEvents
    public void xboxDiscovered() {
        Log.e("widget", "Xbox Discovered");
        this.mBoundService.connect();
    }

    @Override // Interfaces.SmartglassEvents
    public void xboxConnected() {
        Log.e("widget", "Xbox Connected");
        Log.e("RemoteFrag", "Xbox Connected");
        try {
            this.mBoundService.openChannels();
            saveLiveId(this.mBoundService.getLiveId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // Interfaces.SmartglassEvents
    public void xboxDisconnected() {
        Log.e("widget", "Xbox disconnected");
        this.mBoundService.ready = false;
    }

    private void saveLiveId(String str) {
        Log.e("HERE", "saving live id: " + str);
        new EncryptClient(this.mContext.getApplicationContext()).saveValue("liveId", str);
    }

    private String getLiveId(Context context) {
        return new EncryptClient(context.getApplicationContext()).getValue("liveId");
    }
}
