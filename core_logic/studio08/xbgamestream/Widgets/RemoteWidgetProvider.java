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
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import com.google.android.exoplayer2.text.ttml.TtmlNode;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.RewardedAdLoader;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.R;
import network.BindService;
/* loaded from: /app/base.apk/classes3.dex */
public class RemoteWidgetProvider extends AppWidgetProvider implements SmartglassEvents {
    public BindService mBoundService;
    public Context mContext;
    public Boolean mServiceBound = false;
    private ServiceConnection localServiceConnection = new ServiceConnection() { // from class: com.studio08.xbgamestream.Widgets.RemoteWidgetProvider.1
        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            RemoteWidgetProvider.this.mServiceBound = false;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RemoteWidgetProvider.this.mBoundService = ((BindService.MyBinder) iBinder).getService();
            RemoteWidgetProvider.this.mServiceBound = true;
            RemoteWidgetProvider.this.mBoundService.setListener(RemoteWidgetProvider.this);
            RemoteWidgetProvider.this.mBoundService.discover();
        }
    };

    @Override // android.appwidget.AppWidgetProvider
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] iArr) {
        Log.e("here", "ON UPDATE: " + iArr.length);
        updateWidgetViews(context, false, false, false);
    }

    void updateWidgetViews(final Context context, Boolean bool, boolean z, boolean z2) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        final ComponentName componentName = new ComponentName(context, RemoteWidgetProvider.class);
        remoteViews.setOnClickPendingIntent(R.id.widget_connect_button, getPendingSelfIntent(context, "widget_connect_button"));
        remoteViews.setOnClickPendingIntent(R.id.widget_connect_button2, getPendingSelfIntent(context, "widget_connect_button"));
        remoteViews.setOnClickPendingIntent(R.id.widget_connect_button3, getPendingSelfIntent(context, "widget_connect_button"));
        remoteViews.setOnClickPendingIntent(R.id.power, getPendingSelfIntent(context, "power"));
        remoteViews.setOnClickPendingIntent(R.id.home2, getPendingSelfIntent(context, TtmlNode.TEXT_EMPHASIS_MARK_OPEN));
        remoteViews.setOnClickPendingIntent(R.id.home, getPendingSelfIntent(context, TtmlNode.TEXT_EMPHASIS_MARK_OPEN));
        if (bool.booleanValue()) {
            remoteViews.setViewVisibility(R.id.widget_connected, 0);
            addButtonActions(context, remoteViews, false);
        } else {
            addButtonActions(context, remoteViews, true);
            remoteViews.setViewVisibility(R.id.widget_connected, 4);
        }
        if (z) {
            remoteViews.setInt(R.id.status_left, "setBackgroundColor", SupportMenu.CATEGORY_MASK);
            remoteViews.setViewVisibility(R.id.status_left, 0);
            remoteViews.setInt(R.id.status_middle, "setBackgroundColor", SupportMenu.CATEGORY_MASK);
            remoteViews.setViewVisibility(R.id.status_middle, 0);
            remoteViews.setInt(R.id.status_right, "setBackgroundColor", SupportMenu.CATEGORY_MASK);
            remoteViews.setViewVisibility(R.id.status_right, 0);
        }
        if (z2) {
            remoteViews.setInt(R.id.status_left, "setBackgroundColor", -16711936);
            remoteViews.setViewVisibility(R.id.status_left, 0);
            remoteViews.setInt(R.id.status_middle, "setBackgroundColor", -16711936);
            remoteViews.setViewVisibility(R.id.status_middle, 0);
            remoteViews.setInt(R.id.status_right, "setBackgroundColor", -16711936);
            remoteViews.setViewVisibility(R.id.status_right, 0);
            new Handler().postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.Widgets.RemoteWidgetProvider.2
                @Override // java.lang.Runnable
                public void run() {
                    remoteViews.setViewVisibility(R.id.status_left, 4);
                    remoteViews.setViewVisibility(R.id.status_middle, 4);
                    remoteViews.setViewVisibility(R.id.status_right, 4);
                    AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
                }
            }, 2000L);
        }
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }

    public RemoteViews addButtonActions(Context context, RemoteViews remoteViews, Boolean bool) {
        remoteViews.setOnClickPendingIntent(R.id.power, bool.booleanValue() ? getPendingSelfIntent(context, "power") : null);
        remoteViews.setOnClickPendingIntent(R.id.nexus, bool.booleanValue() ? getPendingSelfIntent(context, "nexus") : null);
        remoteViews.setOnClickPendingIntent(R.id.view, bool.booleanValue() ? getPendingSelfIntent(context, "view") : null);
        remoteViews.setOnClickPendingIntent(R.id.dpadRight, bool.booleanValue() ? getPendingSelfIntent(context, "dpadRight") : null);
        remoteViews.setOnClickPendingIntent(R.id.dpadLeft, bool.booleanValue() ? getPendingSelfIntent(context, "dpadLeft") : null);
        remoteViews.setOnClickPendingIntent(R.id.dpadUp, bool.booleanValue() ? getPendingSelfIntent(context, "dpadUp") : null);
        remoteViews.setOnClickPendingIntent(R.id.dpadDown, bool.booleanValue() ? getPendingSelfIntent(context, "dpadDown") : null);
        remoteViews.setOnClickPendingIntent(R.id.a, bool.booleanValue() ? getPendingSelfIntent(context, "a") : null);
        remoteViews.setOnClickPendingIntent(R.id.b, bool.booleanValue() ? getPendingSelfIntent(context, "b") : null);
        remoteViews.setOnClickPendingIntent(R.id.menu, bool.booleanValue() ? getPendingSelfIntent(context, "menu") : null);
        remoteViews.setOnClickPendingIntent(R.id.x, bool.booleanValue() ? getPendingSelfIntent(context, "x") : null);
        remoteViews.setOnClickPendingIntent(R.id.x2, bool.booleanValue() ? getPendingSelfIntent(context, "x") : null);
        remoteViews.setOnClickPendingIntent(R.id.y, bool.booleanValue() ? getPendingSelfIntent(context, "y") : null);
        remoteViews.setOnClickPendingIntent(R.id.y2, bool.booleanValue() ? getPendingSelfIntent(context, "y") : null);
        return remoteViews;
    }

    PendingIntent getPendingSelfIntent(Context context, String str) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(str);
        return PendingIntent.getBroadcast(context, 0, intent, AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL);
    }

    public void sendXboxCommand(Intent intent, Context context) {
        if (intent.getAction() != null) {
            Log.e("here", "Receive value: " + intent.getAction());
            if (intent.getAction().equals("widget_connect_button")) {
                retryConnect(5, context.getApplicationContext());
            } else if (intent.getAction().equals(TtmlNode.TEXT_EMPHASIS_MARK_OPEN)) {
                Intent intent2 = new Intent(context, MainActivity.class);
                intent2.setFlags(268435456);
                context.startActivity(intent2);
            } else if (!TextUtils.isEmpty(intent.getAction()) && !intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS")) {
                sendBroadcast(context, intent.getAction());
                ((Vibrator) context.getSystemService("vibrator")).vibrate(40L, new AudioAttributes.Builder().setContentType(4).setUsage(4).build());
            } else {
                Log.e("here", "Receive BAD command: " + intent.getAction());
            }
        }
    }

    @Override // android.appwidget.AppWidgetProvider, android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e("here", "Receive");
        this.mContext = context;
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
        if (i <= 0) {
            Log.e("HERE", "Error connecting, out of retries!!!!");
            Toast.makeText(context.getApplicationContext(), "Cannot connect to Xbox!", 0).show();
            updateWidgetViews(context, false, true, false);
            return;
        }
        if (TextUtils.isEmpty(getLiveId(context))) {
            Log.e("HERE", "Cant use widget until initial connect");
        }
        if (RewardedAdLoader.shouldShowAd(context.getApplicationContext())) {
            Toast.makeText(context.getApplicationContext(), "View ad to continue", 0).show();
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(268435456);
            intent.putExtra("widgetShowAd", true);
            context.startActivity(intent);
        } else if (bindToService(context)) {
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.Widgets.RemoteWidgetProvider.3
                @Override // java.lang.Runnable
                public void run() {
                    if (RemoteWidgetProvider.this.bindToService(context)) {
                        return;
                    }
                    RemoteWidgetProvider.this.retryConnect(i - 1, context);
                }
            }, 3000L);
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
            updateWidgetViews(context, false, false, true);
            return true;
        }
        updateWidgetViews(context, true, true, false);
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
