package com.studio08.xbgamestream.Converter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.arthenica.ffmpegkit.Statistics;
import com.studio08.xbgamestream.Converter.VideoFormatConverter;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.ScreenCastActivity;
import com.tapjoy.TJAdUnitConstants;
import java.util.ArrayList;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class ConvertForegroundService extends Service implements VideoFormatConverter.VideoConvertEvents {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final int MSG_CONVERT_COMPLETE = 4;
    public static final int MSG_CONVERT_FAILED = 5;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_STOP_SERVICE = 6;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_UPDATE_STATS = 3;
    VideoFormatConverter converter;
    public String filePath;
    NotificationManager manager;
    Notification notification;
    ArrayList<Messenger> mClients = new ArrayList<>();
    boolean isRunning = true;
    public int NOTIFICATION_ID = 4;
    public int NOTIFICATION_STATUS_ID = 5;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /* loaded from: /app/base.apk/classes3.dex */
    class IncomingHandler extends Handler {
        IncomingHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                ConvertForegroundService.this.mClients.add(message.replyTo);
            } else if (i == 2) {
                ConvertForegroundService.this.mClients.remove(message.replyTo);
            } else if (i == 6) {
                ConvertForegroundService.this.isRunning = false;
                Log.w("HEREHERE", "STOPPING SERVICE from MESSAGE");
                ConvertForegroundService.this.converter.cancel();
                ConvertForegroundService.this.stopForeground(true);
                ConvertForegroundService.this.stopSelf();
            } else {
                super.handleMessage(message);
            }
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mMessenger.getBinder();
    }

    private void sendStatsMessage(String str) {
        for (int size = this.mClients.size() - 1; size >= 0; size--) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put(TJAdUnitConstants.String.MESSAGE, str);
                this.mClients.get(size).send(Message.obtain(null, 3, jSONObject));
            } catch (RemoteException unused) {
                this.mClients.remove(size);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCompleteMessage(String str) {
        for (int size = this.mClients.size() - 1; size >= 0; size--) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put(TJAdUnitConstants.String.MESSAGE, str);
                this.mClients.get(size).send(Message.obtain(null, 4, jSONObject));
            } catch (RemoteException unused) {
                this.mClients.remove(size);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendFailedMessage(String str) {
        for (int size = this.mClients.size() - 1; size >= 0; size--) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put(TJAdUnitConstants.String.MESSAGE, str);
                this.mClients.get(size).send(Message.obtain(null, 5, jSONObject));
            } catch (RemoteException e) {
                this.mClients.remove(size);
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        this.filePath = intent.getStringExtra("filePath");
        createNotificationChannel();
        Intent intent2 = new Intent(this, ScreenCastActivity.class);
        PendingIntent activity = PendingIntent.getActivity(this, 0, intent2, 201326592);
        intent2.addFlags(536870912);
        this.notification = new NotificationCompat.Builder(this, "ForegroundServiceChannel").setContentTitle("xbPlay Video Convert Started").setContentText("Converting video...").setSmallIcon(R.drawable.app_notification_icon).setContentIntent(activity).build();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(this.NOTIFICATION_ID, this.notification, 2);
        } else {
            startForeground(this.NOTIFICATION_ID, this.notification);
        }
        convertVideo();
        return 2;
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("ForegroundServiceChannel", "Foreground Service Channel", 3);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            this.manager = notificationManager;
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void convertVideo() {
        VideoFormatConverter videoFormatConverter = new VideoFormatConverter(this.filePath, getApplicationContext());
        this.converter = videoFormatConverter;
        videoFormatConverter.setCustomListener(this);
        this.converter.runFFMpegConvert();
    }

    @Override // com.studio08.xbgamestream.Converter.VideoFormatConverter.VideoConvertEvents
    public void onVideoConvertSuccess(String str) {
        sendCompleteMessage(str);
        Intent intent = new Intent(this, ScreenCastActivity.class);
        intent.putExtra("outputPath", str);
        intent.putExtra("completed", true);
        intent.putExtra("showCastView", true);
        intent.addFlags(536870912);
        intent.addFlags(268435456);
        Notification build = new NotificationCompat.Builder(this, "ForegroundServiceChannel").setContentTitle("xbPlay Video Convert Completed!").setContentText("Saved at:" + str).setSmallIcon(R.drawable.app_notification_icon).setContentIntent(PendingIntent.getActivity(this, 0, intent, 201326592)).build();
        this.notification = build;
        this.manager.notify(this.NOTIFICATION_STATUS_ID, build);
        stopForeground(true);
        stopSelf();
    }

    @Override // com.studio08.xbgamestream.Converter.VideoFormatConverter.VideoConvertEvents
    public void onVideoConvertFailed(final String str) {
        Log.e("HERE", "ConvertForegroundService caught video convert failure");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.Converter.ConvertForegroundService.1
            @Override // java.lang.Runnable
            public void run() {
                ConvertForegroundService.this.sendFailedMessage(str);
            }
        }, 500L);
        if (this.isRunning) {
            Intent intent = new Intent(this, ScreenCastActivity.class);
            intent.putExtra("convertError", str);
            intent.putExtra("showCastView", true);
            intent.addFlags(536870912);
            Notification build = new NotificationCompat.Builder(this, "ForegroundServiceChannel").setContentTitle("xbPlay Video Convert Failed!").setContentText("Error: " + str).setSmallIcon(R.drawable.app_notification_icon).setContentIntent(PendingIntent.getActivity(this, 0, intent, 201326592)).build();
            this.notification = build;
            this.manager.notify(this.NOTIFICATION_STATUS_ID, build);
        }
        stopForeground(true);
        stopSelf();
    }

    @Override // com.studio08.xbgamestream.Converter.VideoFormatConverter.VideoConvertEvents
    public void onStatsUpdated(Statistics statistics, String str) {
        if (this.isRunning) {
            sendStatsMessage("Video converted: " + ((statistics.getSize() / 1000000) + 1) + "MB\nDurations of video converted: " + Helper.formatTime(statistics.getTime()) + "\nResolution Selected: " + this.converter.getResolution() + "\nConstant Rate Factor Selected: " + this.converter.getConvertQualityValue());
            Intent intent = new Intent(this, ScreenCastActivity.class);
            PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 201326592);
            intent.putExtra("outputPath", str);
            intent.putExtra("showCastView", true);
            intent.addFlags(536870912);
            Notification build = new NotificationCompat.Builder(this, "ForegroundServiceChannel").setContentTitle("xbPlay Converting Video...").setContentText("Durations of video converted: " + Helper.formatTime(statistics.getTime()) + " (" + ((statistics.getSize() / 1000000) + 1) + " MB)").setSmallIcon(R.drawable.app_notification_icon).setContentIntent(activity).build();
            this.notification = build;
            this.manager.notify(this.NOTIFICATION_ID, build);
        }
    }
}
