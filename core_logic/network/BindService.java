package network;

import Interfaces.ChannelEvents;
import Interfaces.NanoStreamEvents;
import Interfaces.SmartglassEvents;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.cast.MediaError;
import constants.SystemInputButtonMappings;
import crypto.SgCrypto;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import javax.jmdns.impl.constants.DNSConstants;
import nano.GameController;
import nano.base.Nano;
import nano.packets.response.NanoResponse;
import nano.packets.response.VideoDataResponse;
import org.apache.http.message.TokenParser;
import packet.receive.DiscoverySimpleResponse;
import studio09.gameconrtollerforxbox.R;
import util.Util;
import xbox.Xbox;
/* loaded from: /app/base.apk/classes4.dex */
public class BindService extends Service implements ChannelEvents, NanoStreamEvents {
    public static final String CHANNEL_ID = "RemoteServiceChannel";
    private static String LOG_TAG = "BoundService";
    SmartglassEvents listener;
    String liveId;
    NotificationManager manager;
    MyServiceReceiver myServiceReceiver;

    /* renamed from: nano  reason: collision with root package name */
    Nano f19nano;
    Notification notification;
    String notificationSettings;

    /* renamed from: xbox  reason: collision with root package name */
    Xbox f20xbox;
    private IBinder mBinder = new MyBinder();
    GameController[] gameControllers = new GameController[4];
    public boolean ready = false;
    boolean isBound = false;
    int failedCounter = 0;
    int NOTIFICATION_ID = 6;
    boolean createdNotification = false;
    List<byte[][]> commandSequenceQueue = new ArrayList();

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        Log.e("HERE", "ONSTARTCOMMAND!!!");
        if (intent != null) {
            this.notificationSettings = intent.getStringExtra("notification_type");
        } else if (this.notificationSettings != null) {
            Log.w("HERE", "No intent set on start command, falling back to " + this.notificationSettings);
        } else {
            this.notificationSettings = "close_on_exit";
            Log.e("HERE", "No intent set on start command! And no fallback value!! AHH");
        }
        Log.e("HERE", "Started service with settings: " + this.notificationSettings);
        createNotificationChannel();
        setupNotificationButtons(createNotification());
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(this.NOTIFICATION_ID, this.notification, 2);
        } else {
            startForeground(this.NOTIFICATION_ID, this.notification);
        }
        if (!this.createdNotification) {
            IntentFilter intentFilter = new IntentFilter("buttonPress");
            intentFilter.addAction("buttonPress");
            this.myServiceReceiver = new MyServiceReceiver();
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(this.myServiceReceiver, intentFilter, 2);
            } else {
                registerReceiver(this.myServiceReceiver, intentFilter);
            }
        }
        this.createdNotification = true;
        String str = this.notificationSettings;
        if (str != null && str.equals("never_show")) {
            stopForeground(true);
        }
        return 1;
    }

    public RemoteViews createNotification() {
        new Intent().addFlags(536870912);
        this.notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("xbPlay - Xbox Navigation Remote").setContentText("Swipe down to show").setSmallIcon(R.drawable.app_notification_icon).setOngoing(true).setAutoCancel(false).build();
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.customnotification_dark);
        this.notification.bigContentView = remoteViews;
        return remoteViews;
    }

    private void setupNotificationButtons(RemoteViews remoteViews) {
        remoteViews.setOnClickPendingIntent(R.id.dpadUp, getPendingSelfIntent(getApplicationContext(), "dpadUp", R.id.dpadUp));
        remoteViews.setOnClickPendingIntent(R.id.dpadDown, getPendingSelfIntent(getApplicationContext(), "dpadDown", R.id.dpadDown));
        remoteViews.setOnClickPendingIntent(R.id.dpadLeft, getPendingSelfIntent(getApplicationContext(), "dpadLeft", R.id.dpadLeft));
        remoteViews.setOnClickPendingIntent(R.id.dpadRight, getPendingSelfIntent(getApplicationContext(), "dpadRight", R.id.dpadRight));
        remoteViews.setOnClickPendingIntent(R.id.a, getPendingSelfIntent(getApplicationContext(), "a", R.id.a));
        remoteViews.setOnClickPendingIntent(R.id.b, getPendingSelfIntent(getApplicationContext(), "b", R.id.b));
        remoteViews.setOnClickPendingIntent(R.id.x, getPendingSelfIntent(getApplicationContext(), "x", R.id.x));
        remoteViews.setOnClickPendingIntent(R.id.y, getPendingSelfIntent(getApplicationContext(), "y", R.id.y));
        remoteViews.setOnClickPendingIntent(R.id.nexus, getPendingSelfIntent(getApplicationContext(), "nexus", R.id.nexus));
        remoteViews.setOnClickPendingIntent(R.id.power, getPendingSelfIntent(getApplicationContext(), "power", R.id.power));
        remoteViews.setOnClickPendingIntent(R.id.close, getPendingSelfIntent(getApplicationContext(), "close", R.id.close));
        Intent intent = new Intent();
        intent.addFlags(268435456);
        intent.setPackage(getApplicationContext().getPackageName());
        remoteViews.setOnClickPendingIntent(R.id.home, PendingIntent.getActivity(getApplicationContext(), R.id.home, intent, 201326592));
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String str, int i) {
        Intent intent = new Intent("buttonPress");
        intent.setAction("buttonPress");
        intent.putExtra("buttonValue", str);
        return PendingIntent.getBroadcast(context, i, intent, 201326592);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel", 3);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            this.manager = notificationManager;
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /* loaded from: /app/base.apk/classes4.dex */
    public class MyServiceReceiver extends BroadcastReceiver {
        public MyServiceReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String stringExtra = intent.getStringExtra("buttonValue");
            Log.e("HERE", "received msg: " + action + " " + stringExtra);
            stringExtra.hashCode();
            char c = 65535;
            switch (stringExtra.hashCode()) {
                case -1325629270:
                    if (stringExtra.equals("dpadUp")) {
                        c = 0;
                        break;
                    }
                    break;
                case 97:
                    if (stringExtra.equals("a")) {
                        c = 1;
                        break;
                    }
                    break;
                case 98:
                    if (stringExtra.equals("b")) {
                        c = 2;
                        break;
                    }
                    break;
                case DNSConstants.KNOWN_ANSWER_TTL /* 120 */:
                    if (stringExtra.equals("x")) {
                        c = 3;
                        break;
                    }
                    break;
                case 121:
                    if (stringExtra.equals("y")) {
                        c = 4;
                        break;
                    }
                    break;
                case 3208415:
                    if (stringExtra.equals("home")) {
                        c = 5;
                        break;
                    }
                    break;
                case 3347807:
                    if (stringExtra.equals("menu")) {
                        c = 6;
                        break;
                    }
                    break;
                case 3619493:
                    if (stringExtra.equals("view")) {
                        c = 7;
                        break;
                    }
                    break;
                case 94756344:
                    if (stringExtra.equals("close")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 104715263:
                    if (stringExtra.equals("nexus")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 106858757:
                    if (stringExtra.equals("power")) {
                        c = '\n';
                        break;
                    }
                    break;
                case 399827373:
                    if (stringExtra.equals("dpadRight")) {
                        c = 11;
                        break;
                    }
                    break;
                case 858558297:
                    if (stringExtra.equals("power_on")) {
                        c = '\f';
                        break;
                    }
                    break;
                case 1675054833:
                    if (stringExtra.equals("dpadDown")) {
                        c = TokenParser.CR;
                        break;
                    }
                    break;
                case 1675283030:
                    if (stringExtra.equals("dpadLeft")) {
                        c = 14;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.dpadUp);
                    break;
                case 1:
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.a);
                    break;
                case 2:
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.b);
                    break;
                case 3:
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.x);
                    break;
                case 4:
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.y);
                    break;
                case 5:
                    new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: network.BindService.MyServiceReceiver.2
                        @Override // java.lang.Runnable
                        public void run() {
                        }
                    });
                    break;
                case 6:
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.menu);
                    break;
                case 7:
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.view);
                    break;
                case '\b':
                    BindService.this.stopForeground(true);
                    BindService.this.ready = false;
                    BindService.this.f20xbox.disconnect();
                    BindService.this.stopSelf();
                    new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: network.BindService.MyServiceReceiver.1
                        @Override // java.lang.Runnable
                        public void run() {
                            Toast.makeText(BindService.this.getApplicationContext(), "Navigation remote disconnected", 0).show();
                        }
                    });
                    break;
                case '\t':
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.nexus);
                    break;
                case '\n':
                    BindService.this.powerOff();
                    BindService bindService = BindService.this;
                    bindService.powerOn(bindService.liveId);
                    BindService bindService2 = BindService.this;
                    bindService2.powerOn(bindService2.liveId);
                    BindService bindService3 = BindService.this;
                    bindService3.powerOn(bindService3.liveId);
                    break;
                case 11:
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.dpadRight);
                    break;
                case '\f':
                    BindService bindService4 = BindService.this;
                    bindService4.powerOn(bindService4.liveId);
                    break;
                case '\r':
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.dpadDown);
                    break;
                case 14:
                    BindService.this.sendSystemInputCommand(SystemInputButtonMappings.dpadLeft);
                    break;
                default:
                    Log.e("HERE", "BindService error. Received unrecognized command: " + stringExtra);
                    break;
            }
            Vibrator vibrator = (Vibrator) BindService.this.getSystemService("vibrator");
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(50L);
            } else {
                Log.v("Can Vibrate", "NO");
            }
        }
    }

    public void setListener(SmartglassEvents smartglassEvents) {
        this.listener = smartglassEvents;
        this.f20xbox.setListener(smartglassEvents);
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.e(LOG_TAG, "in onCreate");
        Xbox xbox2 = this.f20xbox;
        if (xbox2 == null || !xbox2.connected) {
            this.f20xbox = new Xbox();
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.e(LOG_TAG, "IN ONBIND");
        this.isBound = true;
        return this.mBinder;
    }

    @Override // android.app.Service
    public void onRebind(Intent intent) {
        Log.e(LOG_TAG, "in onRebind");
        this.isBound = true;
        super.onRebind(intent);
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        Log.e(LOG_TAG, "in onUnbind");
        this.isBound = false;
        String str = this.notificationSettings;
        if (str != null && str.equals("close_on_exit")) {
            stopForeground(true);
            stopSelf();
        }
        return true;
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this.ready = false;
        try {
            unregisterReceiver(this.myServiceReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(LOG_TAG, "in onDestroy");
    }

    public void disconnectXbox() {
        Xbox xbox2;
        this.ready = false;
        Xbox xbox3 = this.f20xbox;
        if (xbox3 == null || !xbox3.connected) {
            Log.e("HERE", "Called xbox disconnect, but not connected");
        } else {
            this.f20xbox.disconnect();
            SmartglassEvents smartglassEvents = this.listener;
            if (smartglassEvents != null) {
                smartglassEvents.xboxDisconnected();
            }
            if (this.isBound && (xbox2 = this.f20xbox) != null) {
                xbox2.discovered = false;
            }
        }
        Xbox xbox4 = this.f20xbox;
        if (xbox4 == null || !(xbox4.connected || this.isBound)) {
            connect();
        }
    }

    public void discover() {
        new Thread(new Runnable() { // from class: network.BindService.1
            @Override // java.lang.Runnable
            public void run() {
                Log.e("Smartglass", "Starting Discover");
                if (BindService.this.isBound && BindService.this.f20xbox != null && BindService.this.f20xbox.connected) {
                    Log.e("Here", "Called discover but xbox already discovered");
                    if (BindService.this.listener != null) {
                        BindService.this.listener.xboxDiscovered();
                        return;
                    }
                    return;
                }
                try {
                    DatagramPacket discover = BindService.this.f20xbox.discover();
                    DiscoverySimpleResponse discoverySimpleResponse = new DiscoverySimpleResponse(Util.copyHeadBytes(discover.getData(), discover.getLength()));
                    SgCrypto sgCrypto = new SgCrypto();
                    sgCrypto.loadSgData(discoverySimpleResponse.cert);
                    BindService.this.f20xbox.f32crypto = sgCrypto;
                    BindService.this.f20xbox.consoleId = Util.hexByteArrayToASCIIString(discoverySimpleResponse.consoleName);
                    BindService.this.f20xbox.uuid = Util.hexByteArrayToASCIIString(discoverySimpleResponse.UUID);
                    BindService.this.f20xbox.liveId = sgCrypto.xboxLiveId;
                    BindService.this.liveId = sgCrypto.xboxLiveId;
                    if (BindService.this.listener != null) {
                        BindService.this.listener.xboxDiscovered();
                    }
                    Log.e("Smartglass", "Finished Discover");
                } catch (Exception e) {
                    Log.e("Smartglass", "Discover Failed!");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void connect() {
        new Thread(new Runnable() { // from class: network.BindService.2
            @Override // java.lang.Runnable
            public void run() {
                Log.e("Smartglass", "Starting Connect");
                if (BindService.this.ready) {
                    Log.e("Smartglass", "Asked to connect but already connected! Ignoring!");
                    if (BindService.this.listener != null) {
                        BindService.this.listener.xboxConnected();
                    }
                }
                try {
                    if (BindService.this.f20xbox == null || !BindService.this.f20xbox.discovered) {
                        Log.e(MediaError.ERROR_TYPE_ERROR, "Xbox connect called before discovered");
                        return;
                    }
                    BindService.this.f20xbox.connect();
                    if (!BindService.this.f20xbox.connected) {
                        Log.e(MediaError.ERROR_TYPE_ERROR, "Error connecting, cannot start local join.");
                        return;
                    }
                    BindService.this.f20xbox.listenForPackets();
                    BindService.this.f20xbox.localJoin();
                    if (BindService.this.listener != null) {
                        BindService.this.listener.xboxConnected();
                    }
                    if (!BindService.this.isBound) {
                        BindService.this.openChannels();
                    }
                    Log.e("Smartglass", "Finished Connect");
                } catch (Exception e) {
                    Log.e(MediaError.ERROR_TYPE_ERROR, "Error Connecting: " + e.getMessage());
                }
            }
        }).start();
    }

    public void openChannels() {
        new Thread(new Runnable() { // from class: network.BindService.3
            @Override // java.lang.Runnable
            public void run() {
                Log.e("Smartglass", "Starting OpenChannels");
                if (BindService.this.ready) {
                    Log.e("Smartglass", "Asked to openChannles but already connected! Ignoring!");
                }
                try {
                    if (BindService.this.f20xbox != null && BindService.this.f20xbox.connected) {
                        BindService.this.f20xbox.openChannels(BindService.this);
                        BindService.this.ready = true;
                    } else {
                        BindService.this.ready = false;
                        Log.e(MediaError.ERROR_TYPE_ERROR, "Xbox openChannel called before connected");
                    }
                } catch (Exception e) {
                    BindService.this.ready = false;
                    Log.e(MediaError.ERROR_TYPE_ERROR, "Error opening channels: " + e.getMessage());
                }
            }
        }).start();
    }

    public void powerOff() {
        new Thread(new Runnable() { // from class: network.BindService.4
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (BindService.this.f20xbox == null || !BindService.this.f20xbox.connected) {
                        Log.e(MediaError.ERROR_TYPE_ERROR, "Xbox powerOff called before connected");
                    } else {
                        BindService.this.f20xbox.powerOff();
                    }
                } catch (Exception e) {
                    Log.e(MediaError.ERROR_TYPE_ERROR, "Error powering off xbox: " + e.getMessage());
                }
            }
        }).start();
    }

    public String getLiveId() {
        Xbox xbox2 = this.f20xbox;
        if (xbox2 != null) {
            return xbox2.liveId;
        }
        return null;
    }

    public void powerOn(final String str) {
        new Thread(new Runnable() { // from class: network.BindService.5
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Xbox xbox2 = new Xbox();
                    xbox2.liveId = str;
                    xbox2.powerOn();
                } catch (Exception e) {
                    Log.e(MediaError.ERROR_TYPE_ERROR, "Error powering off xbox: " + e.getMessage());
                }
            }
        }).start();
    }

    public void sendSystemInputSequence(final byte[][] bArr, final long j) {
        new Thread(new Runnable() { // from class: network.BindService.6
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (BindService.this.f20xbox == null || !BindService.this.f20xbox.connected) {
                        Log.e(MediaError.ERROR_TYPE_ERROR, "Xbox sendSystemInput called before connected: " + BindService.this.failedCounter);
                    } else {
                        BindService.this.commandSequenceQueue.add(bArr);
                        int i = 0;
                        while (BindService.this.f20xbox.isSendingSequence && i < 300) {
                            i++;
                            Log.i("LIB", "Waiting for currently running sequence to finish ");
                            Thread.sleep(200L);
                        }
                        if (i >= 300) {
                            Log.e("LIB", "Somehow sequence thread hung. Killing");
                            return;
                        }
                        BindService.this.f20xbox.isSendingSequence = true;
                        byte[][] remove = BindService.this.commandSequenceQueue.remove(0);
                        for (int i2 = 0; remove != null && i2 < remove.length; i2++) {
                            byte[] bArr2 = remove[i2];
                            Log.e("LIB", "Sending button press at index " + i2);
                            BindService.this.f20xbox.sendSystemInputCommand(bArr2);
                            Thread.sleep(j);
                            BindService.this.f20xbox.sendSystemInputCommand(SystemInputButtonMappings.clear);
                            Thread.sleep(j);
                            Log.e("LIB", "Completed button press iteration at index: " + i2);
                        }
                        BindService.this.f20xbox.isSendingSequence = false;
                        BindService.this.failedCounter = 0;
                        Log.e("LIB", "Completed sending all buttons! Remaining sequences" + BindService.this.commandSequenceQueue.size());
                    }
                } catch (Exception e) {
                    Log.e(MediaError.ERROR_TYPE_ERROR, "Error sending system sequence command: " + e.getMessage() + BindService.this.failedCounter);
                }
                BindService.this.f20xbox.isSendingSequence = false;
            }
        }).start();
    }

    public void sendSystemInputCommand(final byte[] bArr) {
        new Thread(new Runnable() { // from class: network.BindService.7
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (BindService.this.f20xbox == null || !BindService.this.f20xbox.connected) {
                        Log.e(MediaError.ERROR_TYPE_ERROR, "Xbox sendSystemInput called before connected: " + BindService.this.failedCounter);
                        BindService.this.failedCounter++;
                        BindService.this.f20xbox.disconnect();
                        BindService.this.disconnectXbox();
                    } else {
                        BindService.this.f20xbox.sendSystemInputCommand(bArr);
                        Thread.sleep(100L);
                        BindService.this.f20xbox.sendSystemInputCommand(SystemInputButtonMappings.clear);
                        BindService.this.failedCounter = 0;
                    }
                } catch (Exception e) {
                    BindService.this.failedCounter++;
                    Log.e(MediaError.ERROR_TYPE_ERROR, "Error sending system input command: " + e.getMessage() + BindService.this.failedCounter);
                    BindService.this.f20xbox.disconnect();
                    BindService.this.disconnectXbox();
                }
                if (BindService.this.failedCounter <= 3 || BindService.this.isBound) {
                    return;
                }
                try {
                    Log.e("HERE", "sending warning to user due to failed sends: " + BindService.this.failedCounter);
                    new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: network.BindService.7.1
                        @Override // java.lang.Runnable
                        public void run() {
                            Toast.makeText(BindService.this.getApplicationContext(), "Disconnected. Open app and reconnect...", 0).show();
                        }
                    });
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }).start();
    }

    public void connectController(final int i) {
        new Thread(new Runnable() { // from class: network.BindService.8
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Thread.sleep(18000L);
                    if (BindService.this.gameControllers[i] != null) {
                        BindService.this.gameControllers[i].connect();
                    } else {
                        Log.e("BindService", "Error: cannot connect controller before created");
                    }
                } catch (Exception e) {
                    Log.e(MediaError.ERROR_TYPE_ERROR, "Error connecting controller: " + e.getMessage());
                }
            }
        }).start();
    }

    public void pressControllerInputButton(final int i, final int i2) {
        new Thread(new Runnable() { // from class: network.BindService.9
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (BindService.this.gameControllers[i] != null && BindService.this.gameControllers[i].connected) {
                        BindService.this.gameControllers[i].pressInputButton(i2);
                    } else {
                        Log.e("BindService", "Error: cannot press button on invalid controller");
                    }
                } catch (Exception e) {
                    Log.e(MediaError.ERROR_TYPE_ERROR, "Error pressing controller button: " + e.getMessage());
                }
            }
        }).start();
    }

    public void pressControllerAnalogButton(final int i, final int i2, final byte[] bArr, final boolean z) {
        new Thread(new Runnable() { // from class: network.BindService.10
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (BindService.this.gameControllers[i] != null && BindService.this.gameControllers[i].connected) {
                        BindService.this.gameControllers[i].pressAnalogButton(i2, bArr, z);
                    } else {
                        Log.e("BindService", "Error: cannot press button on invalid controller");
                    }
                } catch (Exception e) {
                    Log.e(MediaError.ERROR_TYPE_ERROR, "Error pressing controller button: " + e.getMessage());
                }
            }
        }).start();
    }

    private void initControllers(Nano nano2) {
        this.gameControllers[0] = new GameController(0);
        this.gameControllers[0].setNano(nano2);
        this.gameControllers[1] = new GameController(1);
        this.gameControllers[1].setNano(nano2);
        this.gameControllers[2] = new GameController(2);
        this.gameControllers[2].setNano(nano2);
        this.gameControllers[3] = new GameController(3);
        this.gameControllers[3].setNano(nano2);
    }

    @Override // Interfaces.ChannelEvents
    public void channelReady(byte[] bArr) {
        Log.e("HERE", "Channel Ready: " + Util.byteArrayToHexString(bArr, true));
    }

    @Override // Interfaces.ChannelEvents
    public void channelActive(byte[] bArr) {
        Log.e("HERE", "Channel Active: " + Util.byteArrayToHexString(bArr, true));
    }

    @Override // Interfaces.ChannelEvents
    public void nanoInitialized(Nano nano2) {
        Log.e("HERE", "Nano initialized");
        this.f19nano = nano2;
        nano2.setNanoStreamListener(this);
        this.f19nano.start();
        initControllers(this.f19nano);
    }

    @Override // Interfaces.NanoStreamEvents
    public void rawStreamData(String str, NanoResponse nanoResponse) {
        if (str.equals("Microsoft::Rdp::Dct::Channel::Class::Video")) {
            new VideoDataResponse(nanoResponse.getFullData());
        }
    }

    /* loaded from: /app/base.apk/classes4.dex */
    public class MyBinder extends Binder {
        public MyBinder() {
        }

        public BindService getService() {
            return BindService.this;
        }
    }

    public void sendBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction("buttonPressAck");
        intent.putExtra("buttonValue", "power");
        context.sendBroadcast(intent);
    }
}
