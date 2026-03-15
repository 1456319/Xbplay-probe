package com.studio08.xbgamestream.CastToConsole;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import com.anggrayudi.storage.file.MimeType;
import com.connectsdk.core.MediaInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.ConnectableDeviceListener;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.command.ServiceCommandError;
import com.google.firebase.sessions.settings.RemoteSettings;
import com.studio08.xbgamestream.Helpers.FileHelper;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.PWAMainMenuActivity;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.ScreenCastActivity;
import com.studio08.xbgamestream.Servers.FileServer;
import com.studio08.xbgamestream.Servers.Xbox360FileServer;
import java.io.File;
import java.io.IOException;
import java.util.List;
/* loaded from: /app/base.apk/classes3.dex */
public class CastToConsoleForegroundService extends Service implements ConnectableDeviceListener {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static ConnectableDevice mDevice;
    CastServiceListener castServiceListener;
    public String[] filePaths;
    FileServer fileServer;
    MediaControl mMediaControl;
    NotificationManager manager;
    Notification notification;
    Xbox360FileServer xbox360FileServer;
    boolean AUDIO_CAST_MODE = false;
    private IBinder mBinder = new MyBinder();
    public int NOTIFICATION_ID = 234;
    private int currentPlayIndex = 0;
    private boolean playingCooldown = false;
    private boolean startedPlaying = false;
    public MediaControl.PlayStateListener playStateListener = new MediaControl.PlayStateListener() { // from class: com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService.1
        @Override // com.connectsdk.service.capability.listeners.ErrorListener
        public void onError(ServiceCommandError serviceCommandError) {
            Log.d("HERE", "Playstate Listener error = " + serviceCommandError);
        }

        @Override // com.connectsdk.service.capability.listeners.ResponseListener
        public void onSuccess(MediaControl.PlayStateStatus playStateStatus) {
            Log.d("HERE", "Playstate changed | playState = " + playStateStatus);
            int i = AnonymousClass4.$SwitchMap$com$connectsdk$service$capability$MediaControl$PlayStateStatus[playStateStatus.ordinal()];
            if (i == 1) {
                CastToConsoleForegroundService.this.startedPlaying = true;
            } else if (i != 2) {
            } else {
                CastToConsoleForegroundService.this.playNext();
                CastToConsoleForegroundService.this.startedPlaying = false;
            }
        }
    };

    /* loaded from: /app/base.apk/classes3.dex */
    public interface CastServiceListener {
        void onCastTitleUpdated();

        void onMediaControlCreated(MediaControl mediaControl);
    }

    /* loaded from: /app/base.apk/classes3.dex */
    public static class Constants {

        /* loaded from: /app/base.apk/classes3.dex */
        public interface ACTION {
            public static final String EXIT_ACTION = "action.exit";
            public static final String NEXT_ACTION = "action.next";
            public static final String PREV_ACTION = "action.prev";
        }
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onCapabilityUpdated(ConnectableDevice connectableDevice, List<String> list, List<String> list2) {
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onConnectionFailed(ConnectableDevice connectableDevice, ServiceCommandError serviceCommandError) {
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onPairingRequired(ConnectableDevice connectableDevice, DeviceService deviceService, DeviceService.PairingType pairingType) {
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService$4  reason: invalid class name */
    /* loaded from: /app/base.apk/classes3.dex */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$connectsdk$service$capability$MediaControl$PlayStateStatus;

        static {
            int[] iArr = new int[MediaControl.PlayStateStatus.values().length];
            $SwitchMap$com$connectsdk$service$capability$MediaControl$PlayStateStatus = iArr;
            try {
                iArr[MediaControl.PlayStateStatus.Playing.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$connectsdk$service$capability$MediaControl$PlayStateStatus[MediaControl.PlayStateStatus.Finished.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Constants.ACTION.EXIT_ACTION)) {
                Log.e("HERE", "EXIT_ACTION");
                stopForeground(true);
                stopSelf();
            } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
                Log.e("HERE", "NEXT_ACTION");
                this.startedPlaying = true;
                playNext();
            } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
                Log.e("HERE", "PREV_ACTION");
                this.startedPlaying = true;
                playPrevious();
            }
            return 2;
        }
        if (this.notification == null) {
            beginListeningForPlayback();
            return 2;
        }
        return 2;
    }

    public void startCastingFirstVideo() {
        Log.e("HERE", "startCastingFirstVideo");
        this.currentPlayIndex = 0;
        String currentVideoFile = getCurrentVideoFile();
        if (currentVideoFile != null) {
            startFileServer(currentVideoFile);
            updateNotification(FileHelper.getFileNameFromPath(currentVideoFile) + " (" + (this.currentPlayIndex + 1) + RemoteSettings.FORWARD_SLASH_STRING + this.filePaths.length + ")", "Swipe to expand");
            performCast(FileHelper.getFileNameFromPath(currentVideoFile));
        }
    }

    private Class getNotificationClickIntent() {
        return getSharedPreferences("SettingsSharedPref", 0).getBoolean("pwa_use_legacy_theme_key", false) ? ScreenCastActivity.class : PWAMainMenuActivity.class;
    }

    public void beginListeningForPlayback() {
        createNotificationChannel();
        Intent intent = new Intent(this, getNotificationClickIntent());
        intent.putExtra("audioCastType", this.AUDIO_CAST_MODE);
        intent.putExtra("showCastView", true);
        PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 201326592);
        intent.addFlags(536870912);
        this.notification = new NotificationCompat.Builder(this, "ForegroundServiceChannel").setContentTitle("xbPlay - Cast To Console").setContentText("Waiting for user to cast...").setSmallIcon(R.drawable.app_notification_icon).setContentIntent(activity).build();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(this.NOTIFICATION_ID, this.notification, 2);
        } else {
            startForeground(this.NOTIFICATION_ID, this.notification);
        }
        if (this.filePaths == null || getCurrentVideoFile() == null) {
            return;
        }
        updateNotification(FileHelper.getFileNameFromPath(getCurrentVideoFile()) + " (" + (this.currentPlayIndex + 1) + RemoteSettings.FORWARD_SLASH_STRING + this.filePaths.length + ")", "Swipe to expand");
    }

    private String getNextVideoFile() {
        String[] strArr = this.filePaths;
        if (strArr != null) {
            int i = this.currentPlayIndex;
            if (i < strArr.length - 1) {
                int i2 = i + 1;
                this.currentPlayIndex = i2;
                return strArr[i2];
            }
            Log.d("HERE", "On end of playlist!");
            return null;
        }
        Log.d("HERE", "Tried to call play video before paths set!");
        return null;
    }

    private String getPreviousVideoFile() {
        String[] strArr = this.filePaths;
        if (strArr != null) {
            int i = this.currentPlayIndex;
            if (i > 0) {
                int i2 = i - 1;
                this.currentPlayIndex = i2;
                return strArr[i2];
            }
            Log.d("HERE", "On start of playlist!");
            return null;
        }
        Log.d("HERE", "Tried to call play video before paths set!");
        return null;
    }

    public String getCurrentVideoFile() {
        String[] strArr = this.filePaths;
        if (strArr != null) {
            int i = this.currentPlayIndex;
            if (i <= strArr.length - 1 && i >= 0) {
                return strArr[i];
            }
            Log.d("HERE", "Cant get currently playing track!");
            return null;
        }
        Log.d("HERE", "Tried to call get video before paths set!");
        return null;
    }

    private boolean setCooldownLock() {
        if (!this.startedPlaying) {
            Log.e("HERE", "Tried to call setCooldownLock before video playing. Ignoring");
            return false;
        } else if (this.playingCooldown) {
            Log.e("HERE", "Not playing next video due to cooldown. Ignoring");
            return false;
        } else {
            this.playingCooldown = true;
            new Handler().postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService.2
                @Override // java.lang.Runnable
                public void run() {
                    CastToConsoleForegroundService.this.playingCooldown = false;
                }
            }, 2000L);
            return true;
        }
    }

    public void playNext() {
        String nextVideoFile;
        Log.e("HERE", "playNext");
        if (setCooldownLock() && (nextVideoFile = getNextVideoFile()) != null) {
            startFileServer(nextVideoFile);
            updateNotification(FileHelper.getFileNameFromPath(nextVideoFile) + " (" + (this.currentPlayIndex + 1) + RemoteSettings.FORWARD_SLASH_STRING + this.filePaths.length + ")", "Swipe to expand");
            performCast(FileHelper.getFileNameFromPath(nextVideoFile));
        }
    }

    public void playPrevious() {
        String previousVideoFile;
        Log.e("HERE", "playPrevious");
        if (setCooldownLock() && (previousVideoFile = getPreviousVideoFile()) != null) {
            startFileServer(previousVideoFile);
            updateNotification(FileHelper.getFileNameFromPath(previousVideoFile) + " (" + (this.currentPlayIndex + 1) + RemoteSettings.FORWARD_SLASH_STRING + this.filePaths.length + ")", "Swipe to expand");
            performCast(FileHelper.getFileNameFromPath(previousVideoFile));
        }
    }

    public void setCastServiceListener(CastServiceListener castServiceListener) {
        this.castServiceListener = castServiceListener;
    }

    public void setIndex(int i) {
        Log.e("HERE", "setIndex");
        this.currentPlayIndex = i;
    }

    public void setDevice(ConnectableDevice connectableDevice) {
        Log.e("HERE", "setDevice");
        mDevice = connectableDevice;
    }

    public void setMediaControl(MediaControl mediaControl) {
        Log.e("HERE", "setMediaControl");
        this.mMediaControl = mediaControl;
        mediaControl.subscribePlayState(this.playStateListener);
    }

    public void setFilePaths(String[] strArr, boolean z) {
        Log.e("HERE", "setFilePaths: " + strArr.length + (strArr.length > 0 ? strArr[0] : "no_media"));
        this.filePaths = strArr;
        this.currentPlayIndex = 0;
        this.AUDIO_CAST_MODE = z;
    }

    public MediaControl getMediaControl() {
        return this.mMediaControl;
    }

    public ConnectableDevice getDevice() {
        return mDevice;
    }

    public String[] getFilePaths() {
        String[] strArr = this.filePaths;
        if (strArr == null || strArr.length > 0) {
            return strArr;
        }
        return null;
    }

    public int getCurrentPlayIndex() {
        return this.currentPlayIndex;
    }

    private void startFileServer(String str) {
        FileServer fileServer = this.fileServer;
        if (fileServer != null && fileServer.isAlive()) {
            this.fileServer.setUrl(str);
        } else {
            FileServer fileServer2 = new FileServer(str, !this.AUDIO_CAST_MODE ? "video" : "audio", getApplicationContext());
            this.fileServer = fileServer2;
            try {
                fileServer2.start();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error creating local file server!" + e.getMessage(), 0).show();
                e.printStackTrace();
            }
        }
        Xbox360FileServer xbox360FileServer = this.xbox360FileServer;
        if (xbox360FileServer != null && xbox360FileServer.isRunning()) {
            this.xbox360FileServer.stop();
        }
        Xbox360FileServer xbox360FileServer2 = new Xbox360FileServer(new File(str));
        this.xbox360FileServer = xbox360FileServer2;
        xbox360FileServer2.init(!this.AUDIO_CAST_MODE ? "video/mp4" : MimeType.AUDIO);
        this.xbox360FileServer.start();
    }

    private String getServerUrl() {
        ConnectableDevice connectableDevice = mDevice;
        if (connectableDevice != null && connectableDevice.isConnected() && mDevice.getFriendlyName().contains("360")) {
            Xbox360FileServer xbox360FileServer = this.xbox360FileServer;
            if (xbox360FileServer.isRunning() & (xbox360FileServer != null)) {
                return this.xbox360FileServer.getFileUrl();
            }
            Toast.makeText(getApplicationContext(), "Error sending file. Restart app", 0).show();
            return "No file selected";
        }
        FileServer fileServer = this.fileServer;
        if (fileServer != null && fileServer.isAlive()) {
            return "http://" + Helper.getLocalIpAddress() + ":" + this.fileServer.getListeningPort();
        }
        return "No file selected";
    }

    public void updateNotification(String str, String str2) {
        Intent intent = new Intent(this, getNotificationClickIntent());
        intent.putExtra("audioCastType", this.AUDIO_CAST_MODE);
        intent.putExtra("showCastView", true);
        PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 201326592);
        Intent intent2 = new Intent(this, CastToConsoleForegroundService.class);
        intent2.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent service = PendingIntent.getService(this, 0, intent2, AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL);
        Intent intent3 = new Intent(this, CastToConsoleForegroundService.class);
        intent3.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent service2 = PendingIntent.getService(this, 0, intent3, AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL);
        Intent intent4 = new Intent(this, CastToConsoleForegroundService.class);
        intent4.setAction(Constants.ACTION.EXIT_ACTION);
        PendingIntent service3 = PendingIntent.getService(this, 0, intent4, AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL);
        intent.addFlags(536870912);
        Notification build = new NotificationCompat.Builder(this, "ForegroundServiceChannel").setSmallIcon(R.drawable.app_notification_icon).setContentIntent(activity).setContentTitle(str).setContentText(str2).addAction(17301541, "PREVIOUS", service2).addAction(17301560, "EXIT", service3).addAction(17301538, "NEXT", service).build();
        this.notification = build;
        this.manager.notify(this.NOTIFICATION_ID, build);
    }

    private void performCast(String str) {
        String str2 = "xbPlay - " + str;
        String str3 = !this.AUDIO_CAST_MODE ? MimeType.VIDEO : MimeType.AUDIO;
        String serverUrl = getServerUrl();
        Log.e("HERE", "Casting: " + serverUrl);
        try {
            mDevice.getMediaPlayer().playMedia(new MediaInfo.Builder(serverUrl, str3).setTitle(str2).setDescription("Casting from xbPlay").setIcon("https://d1o4538xtdh4nm.cloudfront.net/xb_gamestream_icon.png").build(), false, new MediaPlayer.LaunchListener() { // from class: com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService.3
                @Override // com.connectsdk.service.capability.listeners.ResponseListener
                public void onSuccess(MediaPlayer.MediaLaunchObject mediaLaunchObject) {
                    CastToConsoleForegroundService.this.setMediaControl(mediaLaunchObject.mediaControl);
                    if (CastToConsoleForegroundService.this.castServiceListener != null) {
                        CastToConsoleForegroundService.this.castServiceListener.onMediaControlCreated(mediaLaunchObject.mediaControl);
                    }
                }

                @Override // com.connectsdk.service.capability.listeners.ErrorListener
                public void onError(ServiceCommandError serviceCommandError) {
                    Log.d("App Tag", "Play media failure: " + serviceCommandError);
                }
            });
        } catch (Exception unused) {
            Toast.makeText(getApplicationContext(), "Error Casting. Please restart", 0).show();
        }
        CastServiceListener castServiceListener = this.castServiceListener;
        if (castServiceListener != null) {
            castServiceListener.onCastTitleUpdated();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("ForegroundServiceChannel", "CastForegroundServiceChannel", 3);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            this.manager = notificationManager;
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void stopServiceCommand() {
        stopSelf();
        stopForeground(true);
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onDeviceReady(ConnectableDevice connectableDevice) {
        mDevice = connectableDevice;
        this.mMediaControl = null;
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onDeviceDisconnected(ConnectableDevice connectableDevice) {
        mDevice = null;
    }

    /* loaded from: /app/base.apk/classes3.dex */
    public class MyBinder extends Binder {
        public MyBinder() {
        }

        public CastToConsoleForegroundService getService() {
            return CastToConsoleForegroundService.this;
        }
    }
}
