package com.studio08.xbgamestream;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.navigation.ui.AppBarConfiguration;
import com.anggrayudi.storage.file.MimeType;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.ConnectableDeviceListener;
import com.connectsdk.device.DevicePicker;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.PlaylistControl;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.sessions.settings.RemoteSettings;
import com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService;
import com.studio08.xbgamestream.Converter.ConvertForegroundService;
import com.studio08.xbgamestream.Helpers.FileHelper;
import com.studio08.xbgamestream.Helpers.FirebaseAnalyticsClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.tapjoy.TJAdUnitConstants;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class ScreenCastActivity extends AppCompatActivity implements ConnectableDeviceListener, DiscoveryManagerListener, CastToConsoleForegroundService.CastServiceListener {
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private static ConnectableDevice mDevice;
    private static DiscoveryManager mDiscoveryManager;
    public FirebaseAnalyticsClient analyticsClient;
    private AppBarConfiguration appBarConfiguration;
    CastToConsoleForegroundService castBindService;
    TextView infoText;
    boolean mIsBound;
    MediaControl mMediaControl;
    PlaylistControl mPlaylistControl;
    ProgressDialog progressDialog;
    Timer refreshTimer;
    StreamWebview streamView;
    private boolean AUDIO_CAST_MODE = false;
    private int PICKFILE_RESULT_CODE = 1235;
    int MEDIA_CURRENT_TIME = 0;
    long MEDIA_DURATION = 0;
    int VOLUME_LEVEL = 100;
    boolean isMute = false;
    Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    Boolean castServiceBound = false;
    private ServiceConnection mConnection = new ServiceConnection() { // from class: com.studio08.xbgamestream.ScreenCastActivity.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ScreenCastActivity.this.mService = new Messenger(iBinder);
            Log.e("HERE", "Attached.");
            try {
                Message obtain = Message.obtain((Handler) null, 1);
                obtain.replyTo = ScreenCastActivity.this.mMessenger;
                ScreenCastActivity.this.mService.send(obtain);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.e("HERE", "Remote service connected.");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            ScreenCastActivity.this.mService = null;
            Log.e("HERE", "Disconnected.");
        }
    };
    private ServiceConnection castToConsoleForegroundServiceBindConnection = new ServiceConnection() { // from class: com.studio08.xbgamestream.ScreenCastActivity.2
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("HERE", "castToConsoleForegroundServiceBindConnection Attached.");
            ScreenCastActivity.this.castBindService = ((CastToConsoleForegroundService.MyBinder) iBinder).getService();
            ScreenCastActivity.this.castServiceBound = true;
            if (ScreenCastActivity.this.mMediaControl != null || ScreenCastActivity.this.castBindService.getMediaControl() == null) {
                Log.e("HERE", "Cast not running in service yet. Unable to load media control object" + (ScreenCastActivity.this.mMediaControl == null) + " - " + (ScreenCastActivity.this.castBindService.getMediaControl() != null));
            } else {
                Log.e("HERE", "Hydrating media control element from service!");
                ScreenCastActivity screenCastActivity = ScreenCastActivity.this;
                screenCastActivity.mMediaControl = screenCastActivity.castBindService.getMediaControl();
                ScreenCastActivity.this.mMediaControl.subscribePlayState(ScreenCastActivity.this.playStateListener);
                ScreenCastActivity.this.startUpdating();
            }
            if (ScreenCastActivity.mDevice == null && ScreenCastActivity.this.castBindService.getDevice() != null) {
                ConnectableDevice unused = ScreenCastActivity.mDevice = ScreenCastActivity.this.castBindService.getDevice();
            }
            ScreenCastActivity.this.castBindService.setCastServiceListener(ScreenCastActivity.this);
            ScreenCastActivity.this.updateInfoText();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("HERE", "Disconnected.");
            ScreenCastActivity.this.castServiceBound = false;
        }
    };
    ApiClient.StreamingClientListener buttonPressListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.3
        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void genericMessage(String str, String str2) {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void onCloseScreenDetected() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void onReLoginDetected() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void setOrientationValue(String str) {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void vibrate() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void pressButtonWifiRemote(String str) {
            ScreenCastActivity.this.sendCastRemoteCommand(str);
        }
    };
    ResponseListener castRemoteResponseListener = new ResponseListener<Object>() { // from class: com.studio08.xbgamestream.ScreenCastActivity.4
        @Override // com.connectsdk.service.capability.listeners.ResponseListener
        public void onSuccess(Object obj) {
        }

        @Override // com.connectsdk.service.capability.listeners.ErrorListener
        public void onError(ServiceCommandError serviceCommandError) {
            Toast.makeText(ScreenCastActivity.this, "Error sending command", 0).show();
            serviceCommandError.printStackTrace();
        }
    };
    public MediaControl.PlayStateListener playStateListener = new MediaControl.PlayStateListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.17
        @Override // com.connectsdk.service.capability.listeners.ErrorListener
        public void onError(ServiceCommandError serviceCommandError) {
            Log.d("HERE", "Playstate Listener error = " + serviceCommandError);
        }

        @Override // com.connectsdk.service.capability.listeners.ResponseListener
        public void onSuccess(MediaControl.PlayStateStatus playStateStatus) {
            Log.d("HERE", "Playstate changed | playState = " + playStateStatus);
            int i = AnonymousClass23.$SwitchMap$com$connectsdk$service$capability$MediaControl$PlayStateStatus[playStateStatus.ordinal()];
            if (i == 1) {
                ScreenCastActivity.this.startUpdating();
                if (ScreenCastActivity.this.mMediaControl == null || !ScreenCastActivity.mDevice.hasCapability(MediaControl.Duration)) {
                    return;
                }
                ScreenCastActivity.this.mMediaControl.getDuration(ScreenCastActivity.this.durationListener);
            } else if (i != 2) {
            } else {
                ((TextView) ScreenCastActivity.this.findViewById(R.id.left_seek_tv)).setText("--:--");
                ((TextView) ScreenCastActivity.this.findViewById(R.id.right_seek_tv)).setText("--:--");
                ((SeekBar) ScreenCastActivity.this.findViewById(R.id.seekbar)).setProgress(0);
                ScreenCastActivity.this.stopUpdating();
            }
        }
    };
    public SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.18
        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setSecondaryProgress(0);
            ScreenCastActivity.this.onSeekBarMoved(seekBar.getProgress());
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBar.setSecondaryProgress(seekBar.getProgress());
            ScreenCastActivity.this.stopUpdating();
        }
    };
    private MediaControl.PositionListener positionListener = new MediaControl.PositionListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.21
        @Override // com.connectsdk.service.capability.listeners.ErrorListener
        public void onError(ServiceCommandError serviceCommandError) {
        }

        @Override // com.connectsdk.service.capability.listeners.ResponseListener
        public void onSuccess(Long l) {
            ((TextView) ScreenCastActivity.this.findViewById(R.id.left_seek_tv)).setText("" + Helper.formatTime(l.intValue()));
            ((SeekBar) ScreenCastActivity.this.findViewById(R.id.seekbar)).setProgress(l.intValue());
            ScreenCastActivity.this.MEDIA_CURRENT_TIME = l.intValue();
        }
    };
    private MediaControl.DurationListener durationListener = new MediaControl.DurationListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.22
        @Override // com.connectsdk.service.capability.listeners.ErrorListener
        public void onError(ServiceCommandError serviceCommandError) {
        }

        @Override // com.connectsdk.service.capability.listeners.ResponseListener
        public void onSuccess(Long l) {
            ScreenCastActivity.this.MEDIA_DURATION = l.longValue();
            if (((SeekBar) ScreenCastActivity.this.findViewById(R.id.seekbar)) == null || ((TextView) ScreenCastActivity.this.findViewById(R.id.right_seek_tv)) == null) {
                return;
            }
            ((SeekBar) ScreenCastActivity.this.findViewById(R.id.seekbar)).setMax(l.intValue());
            ((TextView) ScreenCastActivity.this.findViewById(R.id.right_seek_tv)).setText("" + Helper.formatTime(l.intValue()));
        }
    };

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onCapabilityUpdated(ConnectableDevice connectableDevice, List<String> list, List<String> list2) {
    }

    @Override // com.connectsdk.discovery.DiscoveryManagerListener
    public void onDeviceRemoved(DiscoveryManager discoveryManager, ConnectableDevice connectableDevice) {
    }

    @Override // com.connectsdk.discovery.DiscoveryManagerListener
    public void onDeviceUpdated(DiscoveryManager discoveryManager, ConnectableDevice connectableDevice) {
    }

    @Override // com.connectsdk.discovery.DiscoveryManagerListener
    public void onDiscoveryFailed(DiscoveryManager discoveryManager, ServiceCommandError serviceCommandError) {
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onPairingRequired(ConnectableDevice connectableDevice, DeviceService deviceService, DeviceService.PairingType pairingType) {
    }

    @Override // com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService.CastServiceListener
    public void onMediaControlCreated(MediaControl mediaControl) {
        if (this.mMediaControl == null) {
            this.mMediaControl = mediaControl;
            mediaControl.subscribePlayState(this.playStateListener);
        }
    }

    private void setupGoogleAnalytics() {
        try {
            this.analyticsClient = new FirebaseAnalyticsClient(FirebaseAnalytics.getInstance(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService.CastServiceListener
    public void onCastTitleUpdated() {
        updateInfoText();
    }

    /* loaded from: /app/base.apk/classes3.dex */
    class IncomingHandler extends Handler {
        IncomingHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            try {
                int i = message.what;
                if (i == 3) {
                    String string = ((JSONObject) message.obj).getString(TJAdUnitConstants.String.MESSAGE);
                    Log.e("HERE", "Got Stats Data" + string);
                    ScreenCastActivity.this.updateConvertDialogStatus(string);
                } else if (i == 4) {
                    String string2 = ((JSONObject) message.obj).getString(TJAdUnitConstants.String.MESSAGE);
                    Log.e("HERE", "Got Complete Data" + string2);
                    ScreenCastActivity.this.showConvertCompleteDialog(string2);
                } else if (i == 5) {
                    Log.e("HERE", "Screencast Activity caught video convert failure");
                    String string3 = ((JSONObject) message.obj).getString(TJAdUnitConstants.String.MESSAGE);
                    Log.e("HERE", "Got Failed Data" + string3);
                    ScreenCastActivity.this.showConvertFailedDialog(string3);
                } else {
                    super.handleMessage(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void doBindConvertService() {
        bindService(new Intent(this, ConvertForegroundService.class), this.mConnection, 1);
        this.mIsBound = true;
        Log.e("HERE", "Binding convert service");
    }

    void doUnbindConvertService() {
        if (this.mIsBound) {
            if (this.mService != null) {
                try {
                    Message obtain = Message.obtain((Handler) null, 2);
                    obtain.replyTo = this.mMessenger;
                    this.mService.send(obtain);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            unbindService(this.mConnection);
            this.mIsBound = false;
            Log.e("HERE", "Unbinding convert service");
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        Log.e("ScreenCastActivity", "Destroyed");
        try {
            mDiscoveryManager.stop();
            mDiscoveryManager = null;
            doUnbindConvertService();
            if (this.castServiceBound.booleanValue() && this.mMediaControl == null) {
                this.castBindService.stopServiceCommand();
            }
            stopUpdating();
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_screen_cast);
        setOrientationPortrait();
        setupGoogleAnalytics();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Video Cast");
        setSupportActionBar(toolbar);
        this.infoText = (TextView) findViewById(R.id.text_cast_footer);
        DiscoveryManager.init(this);
        DiscoveryManager discoveryManager = DiscoveryManager.getInstance();
        mDiscoveryManager = discoveryManager;
        discoveryManager.addListener(this);
        mDiscoveryManager.start();
        ((SeekBar) findViewById(R.id.seekbar)).setOnSeekBarChangeListener(this.seekListener);
        StreamWebview streamWebview = (StreamWebview) findViewById(R.id.webview1);
        this.streamView = streamWebview;
        streamWebview.setBackgroundColor(0);
        this.streamView.init();
        findViewById(R.id.seekbar_layout).setVisibility(4);
        findViewById(R.id.cast_connect_button).setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ScreenCastActivity.this.selectDevice();
            }
        });
        findViewById(R.id.cast_file_choose_button).setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ScreenCastActivity.this.selectFile();
            }
        });
        findViewById(R.id.cast_send_button).setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ScreenCastActivity.this.analyticsClient.logButtonClickEvent("cast_to_console");
                ScreenCastActivity.this.castToConsole(true);
            }
        });
        findViewById(R.id.cast_remote_button).setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.8
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ScreenCastActivity.this.showRemoteView();
            }
        });
        if (getIntent().getBooleanExtra("showRemoteView", false)) {
            showRemoteView();
        } else if (getIntent().getBooleanExtra("showCastView", false)) {
            setCastType(getIntent());
            showCastView();
        }
        startForegroundCastService();
    }

    public void setOrientationPortrait() {
        String string = getSharedPreferences("SettingsSharedPref", 0).getString("orientation_key", "auto");
        if (string.equals("auto")) {
            setRequestedOrientation(1);
        } else if (string.equals("full_sensor")) {
            setRequestedOrientation(10);
        }
    }

    public void setOrientationLandscape() {
        String string = getSharedPreferences("SettingsSharedPref", 0).getString("orientation_key", "auto");
        if (string.equals("auto")) {
            setRequestedOrientation(0);
        } else if (string.equals("full_sensor")) {
            setRequestedOrientation(10);
        }
    }

    private void setCastType(Intent intent) {
        if (intent == null || !intent.getBooleanExtra("audioCastType", false)) {
            Log.e("HERE", this.AUDIO_CAST_MODE + " - AUDIO_CAST_MODE");
            this.AUDIO_CAST_MODE = false;
            return;
        }
        Log.e("HERE", this.AUDIO_CAST_MODE + " - AUDIO_CAST_MODE");
        this.AUDIO_CAST_MODE = true;
    }

    void showConvertFailedDialog(String str) {
        new AlertDialog.Builder(this).setTitle("Failed to Convert Video").setMessage("Failed to convert video to Xbox 360 compatible format. Please report this error if there is not an obvious solution!\n\n Details: " + str).setCancelable(true).setPositiveButton("Exit", (DialogInterface.OnClickListener) null).show();
        ProgressDialog progressDialog = this.progressDialog;
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        this.progressDialog.dismiss();
    }

    void updateConvertDialogStatus(String str) {
        ProgressDialog progressDialog = this.progressDialog;
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        createConvertProgressDialog(str);
    }

    void showConvertCompleteDialog(final String str) {
        try {
            new AlertDialog.Builder(this).setTitle("Video Converted!").setMessage("Video successfully converted to a format that is compatible with an Xbox 360!\n\nYou can view, recast, or delete this video anytime. It is stored on your device at the location of: " + str).setCancelable(true).setPositiveButton("Cast Now", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.10
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (ScreenCastActivity.this.castBindService != null) {
                        ScreenCastActivity.this.castBindService.setFilePaths(new String[]{str}, false);
                        ScreenCastActivity.this.updateInfoText();
                    } else {
                        Toast.makeText(ScreenCastActivity.this, "Error. Restart App", 0).show();
                    }
                    ScreenCastActivity.this.castToConsole(false);
                }
            }).setNegativeButton(HTTP.CONN_CLOSE, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.9
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    ScreenCastActivity.this.stopService();
                }
            }).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ProgressDialog progressDialog = this.progressDialog;
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        this.progressDialog.dismiss();
    }

    void createConvertProgressDialog(String str) {
        if (this.progressDialog == null) {
            this.progressDialog = new ProgressDialog(this);
        }
        this.progressDialog.setTitle("Converting Video to Valid Xbox 360 Format");
        this.progressDialog.setMessage(str);
        this.progressDialog.setProgressStyle(0);
        this.progressDialog.setCancelable(true);
        this.progressDialog.setButton(-1, "Run In Background", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.11
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                ScreenCastActivity.this.progressDialog.dismiss();
            }
        });
        this.progressDialog.setButton(-2, "Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.12
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                ScreenCastActivity.this.progressDialog.dismiss();
                ScreenCastActivity.this.stopService();
            }
        });
        try {
            this.progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendCastRemoteCommand(final String str) {
        runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.ScreenCastActivity.13
            /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:67:0x01c0 -> B:72:0x01da). Please submit an issue!!! */
            @Override // java.lang.Runnable
            public void run() {
                char c;
                ScreenCastActivity screenCastActivity;
                try {
                    String str2 = str;
                    switch (str2.hashCode()) {
                        case -1850529456:
                            if (str2.equals("Return")) {
                                c = '\n';
                                break;
                            }
                            c = 65535;
                            break;
                        case -1850451749:
                            if (str2.equals("Rewind")) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case -1805124779:
                            if (str2.equals("Volumeup")) {
                                c = '\t';
                                break;
                            }
                            c = 65535;
                            break;
                        case -1209131241:
                            if (str2.equals("Previous")) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case -892960055:
                            if (str2.equals("Fastforward")) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        case 2410041:
                            if (str2.equals("Mute")) {
                                c = 7;
                                break;
                            }
                            c = 65535;
                            break;
                        case 2424595:
                            if (str2.equals("Next")) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case 2490196:
                            if (str2.equals("Play")) {
                                c = 1;
                                break;
                            }
                            c = 65535;
                            break;
                        case 2587682:
                            if (str2.equals("Stop")) {
                                c = 2;
                                break;
                            }
                            c = 65535;
                            break;
                        case 76887510:
                            if (str2.equals("Pause")) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        case 441371356:
                            if (str2.equals("Volumedown")) {
                                c = '\b';
                                break;
                            }
                            c = 65535;
                            break;
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            ScreenCastActivity.this.mMediaControl.pause(ScreenCastActivity.this.castRemoteResponseListener);
                            break;
                        case 1:
                            ScreenCastActivity.this.mMediaControl.play(ScreenCastActivity.this.castRemoteResponseListener);
                            break;
                        case 2:
                            ScreenCastActivity.this.mMediaControl.stop(ScreenCastActivity.this.castRemoteResponseListener);
                            break;
                        case 3:
                            ScreenCastActivity.this.mMediaControl.seek(ScreenCastActivity.this.MEDIA_CURRENT_TIME - 20000, null);
                            break;
                        case 4:
                            ScreenCastActivity.this.mMediaControl.seek(ScreenCastActivity.this.MEDIA_CURRENT_TIME + AccessibilityNodeInfoCompat.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_MAX_LENGTH, null);
                            break;
                        case 5:
                            ScreenCastActivity.this.castBindService.playNext();
                            break;
                        case 6:
                            ScreenCastActivity.this.castBindService.playPrevious();
                            break;
                        case 7:
                            ScreenCastActivity.this.isMute = !screenCastActivity.isMute;
                            ScreenCastActivity.mDevice.getVolumeControl().setMute(ScreenCastActivity.this.isMute, new ResponseListener<Object>() { // from class: com.studio08.xbgamestream.ScreenCastActivity.13.1
                                @Override // com.connectsdk.service.capability.listeners.ResponseListener
                                public void onSuccess(Object obj) {
                                    Log.e("", "");
                                }

                                @Override // com.connectsdk.service.capability.listeners.ErrorListener
                                public void onError(ServiceCommandError serviceCommandError) {
                                    Log.e("", "");
                                }
                            });
                            break;
                        case '\b':
                            if (ScreenCastActivity.this.VOLUME_LEVEL >= 10) {
                                ScreenCastActivity.this.VOLUME_LEVEL -= 10;
                                Toast.makeText(ScreenCastActivity.this, ScreenCastActivity.this.VOLUME_LEVEL + "%", 0).show();
                            }
                            ScreenCastActivity.mDevice.getVolumeControl().setMute(false, null);
                            ScreenCastActivity.mDevice.getVolumeControl().setVolume((float) (ScreenCastActivity.this.VOLUME_LEVEL / 100.0d), null);
                            break;
                        case '\t':
                            if (ScreenCastActivity.this.VOLUME_LEVEL <= 90) {
                                ScreenCastActivity.this.VOLUME_LEVEL += 10;
                                Toast.makeText(ScreenCastActivity.this, ScreenCastActivity.this.VOLUME_LEVEL + "%", 0).show();
                            }
                            ScreenCastActivity.mDevice.getVolumeControl().setMute(false, null);
                            ScreenCastActivity.mDevice.getVolumeControl().setVolume((float) (ScreenCastActivity.this.VOLUME_LEVEL / 100.0d), null);
                            break;
                        case '\n':
                            ScreenCastActivity.this.showCastView();
                            break;
                        default:
                            Toast.makeText(ScreenCastActivity.this, "Button not mapped", 0).show();
                            break;
                    }
                    try {
                        Vibrator vibrator = (Vibrator) ScreenCastActivity.this.getSystemService("vibrator");
                        if (vibrator.hasVibrator()) {
                            vibrator.vibrate(50L);
                        } else {
                            Log.v("Can Vibrate", "NO");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    Log.e("HERE", "Error sending cast remote command");
                    Toast.makeText(ScreenCastActivity.this, "Error sending command", 0).show();
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void selectDevice() {
        new DevicePicker(this).getPickerDialog("Loading Consoles...", new AdapterView.OnItemClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.14
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                ConnectableDevice unused = ScreenCastActivity.mDevice = (ConnectableDevice) adapterView.getItemAtPosition(i);
                ScreenCastActivity.mDevice.addListener(ScreenCastActivity.this);
                ScreenCastActivity.mDevice.connect();
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void selectFile() {
        ConnectableDevice connectableDevice = mDevice;
        if (connectableDevice == null || !connectableDevice.isConnected()) {
            Toast.makeText(this, "Select an Xbox first!", 0).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (!Helper.checkIfAlreadyHavePermission("android.permission.READ_MEDIA_VIDEO", this) || !Helper.checkIfAlreadyHavePermission("android.permission.READ_MEDIA_AUDIO", this)) {
                try {
                    Helper.requestForSpecificPermission(new String[]{"android.permission.READ_MEDIA_VIDEO", "android.permission.READ_MEDIA_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.POST_NOTIFICATIONS"}, this);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        } else if (!Helper.checkIfAlreadyHavePermission("android.permission.READ_EXTERNAL_STORAGE", this) || !Helper.checkIfAlreadyHavePermission("android.permission.WRITE_EXTERNAL_STORAGE", this)) {
            try {
                Helper.requestForSpecificPermission(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, this);
                return;
            } catch (Exception e2) {
                e2.printStackTrace();
                return;
            }
        }
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType(!this.AUDIO_CAST_MODE ? MimeType.VIDEO : MimeType.AUDIO);
        intent.addCategory("android.intent.category.OPENABLE");
        intent.putExtra("android.intent.extra.LOCAL_ONLY", true);
        intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
        startActivityForResult(Intent.createChooser(intent, !this.AUDIO_CAST_MODE ? "Choose a video" : "Choose an audio file"), this.PICKFILE_RESULT_CODE);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void castToConsole(boolean z) {
        ConnectableDevice connectableDevice = mDevice;
        if (connectableDevice == null || !connectableDevice.isConnected()) {
            Toast.makeText(this, "Select a console first", 1).show();
        } else if (this.castBindService.getFilePaths() == null) {
            Toast.makeText(this, "Choose a valid file first", 1).show();
        } else if (z && !this.AUDIO_CAST_MODE) {
            handle360FileConvert();
        } else {
            performCast();
        }
    }

    private void performCast() {
        if (this.castServiceBound.booleanValue()) {
            this.castBindService.setIndex(0);
            this.castBindService.beginListeningForPlayback();
            this.castBindService.startCastingFirstVideo();
            return;
        }
        Toast.makeText(this, "Error Casting. Please restart", 0).show();
    }

    private void handle360FileConvert() {
        if (mDevice.getFriendlyName().contains("360") && this.castBindService.getFilePaths().length != 1) {
            castToConsole(false);
            Toast.makeText(this, "Notice: 360 video convert feature not available when selecting multiple files", 1).show();
            Toast.makeText(this, "If you get 'Playback Error', consider converting video format by selecting individual files.", 1).show();
        } else if (mDevice.getFriendlyName().contains("360") && this.castBindService.getFilePaths().length == 1 && !this.castBindService.getFilePaths()[0].contains("converted")) {
            String string = getSharedPreferences("SettingsSharedPref", 0).getString("video_convert_prompt_key", "ask");
            if (string.equals("ask")) {
                new AlertDialog.Builder(this).setTitle("Warning: Xbox 360 Detected").setMessage("Xbox 360's do not support most new video formats! You can try to cast this video, but it might show 'Playback Error' on your 360.\n\nIf this happens, this app has the ability to convert your video to a format that the Xbox 360 can play! Would you like to convert the video now?\n\nNote, converting videos is quite slow. Consider going into the settings and lowering the video conversion quality to increase the speed of the conversion process.").setCancelable(true).setPositiveButton("Convert", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.16
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ScreenCastActivity.this.convertFileBackground();
                    }
                }).setNegativeButton("Continue without converting", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ScreenCastActivity.15
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ScreenCastActivity.this.castToConsole(false);
                    }
                }).show();
            } else if (string.equals("no_convert")) {
                castToConsole(false);
            } else {
                convertFileBackground();
            }
        } else {
            castToConsole(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void convertFileBackground() {
        this.analyticsClient.logButtonClickEvent("convert_360_video");
        createConvertProgressDialog("Starting...");
        startService();
    }

    private void handleNotificationClick() {
        String stringExtra = getIntent().getStringExtra("outputPath");
        boolean booleanExtra = getIntent().getBooleanExtra("completed", false);
        String stringExtra2 = getIntent().getStringExtra("convertError");
        if (stringExtra != null && booleanExtra) {
            Log.e("HERE", "Convert completed! Showing dialog");
            showConvertCompleteDialog(stringExtra);
        } else if (stringExtra != null) {
            Log.e("HERE", "Convert in progress. Updating text");
        } else if (stringExtra2 != null) {
            showConvertFailedDialog(stringExtra2);
        }
    }

    public void showRemoteView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Cast Media Remote");
        setSupportActionBar(toolbar);
        if (this.mMediaControl == null) {
            Toast.makeText(this, "Nothing playing. Cast something first!", 1).show();
        }
        ApiClient apiClient = new ApiClient(this, this.streamView);
        apiClient.setCustomObjectListener(this.buttonPressListener);
        apiClient.doCastRemote();
        findViewById(R.id.cast_connect_button).setVisibility(4);
        findViewById(R.id.cast_file_choose_button).setVisibility(4);
        findViewById(R.id.cast_send_button).setVisibility(4);
        findViewById(R.id.cast_remote_button).setVisibility(4);
        findViewById(R.id.webview1).setVisibility(0);
        findViewById(R.id.seekbar_layout).setVisibility(0);
    }

    public void showCastView() {
        Log.e("HERE", "showCastView: " + this.AUDIO_CAST_MODE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(!this.AUDIO_CAST_MODE ? "Video Cast" : "Audio Cast");
        setSupportActionBar(toolbar);
        findViewById(R.id.cast_connect_button).setVisibility(0);
        findViewById(R.id.cast_file_choose_button).setVisibility(0);
        ((Button) findViewById(R.id.cast_file_choose_button)).setText(!this.AUDIO_CAST_MODE ? "2. Choose Video File" : "2. Choose Audio File");
        findViewById(R.id.cast_send_button).setVisibility(0);
        findViewById(R.id.cast_remote_button).setVisibility(0);
        findViewById(R.id.webview1).setVisibility(4);
        findViewById(R.id.seekbar_layout).setVisibility(4);
        handleNotificationClick();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateInfoText() {
        String str;
        String currentVideoFile;
        String str2;
        ConnectableDevice connectableDevice = mDevice;
        if (connectableDevice != null && connectableDevice.isConnected()) {
            str = "Connected to device: " + mDevice.getFriendlyName();
        } else {
            str = "Connected to device: N/A";
        }
        if (this.castBindService.getCurrentVideoFile() != null) {
            str2 = "File: " + FileHelper.getFileNameFromPath(currentVideoFile) + " (" + (this.castBindService.getCurrentPlayIndex() + 1) + RemoteSettings.FORWARD_SLASH_STRING + this.castBindService.getFilePaths().length + ")";
        } else {
            str2 = "File: N/A";
        }
        this.infoText.setText(str + "\n " + str2);
    }

    /* JADX WARN: Removed duplicated region for block: B:25:0x0067  */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void onActivityResult(int r4, int r5, android.content.Intent r6) {
        /*
            r3 = this;
            super.onActivityResult(r4, r5, r6)
            r0 = -1
            if (r5 == r0) goto L7
            return
        L7:
            int r5 = r3.PICKFILE_RESULT_CODE
            if (r4 != r5) goto L73
            r4 = 1
            r5 = 0
            java.lang.String[] r0 = new java.lang.String[r5]     // Catch: java.lang.Exception -> L61
            android.net.Uri r1 = r6.getData()     // Catch: java.lang.Exception -> L61
            java.lang.String r2 = "HERE"
            if (r1 == 0) goto L2a
            java.lang.String[] r0 = new java.lang.String[r4]     // Catch: java.lang.Exception -> L61
            android.net.Uri r6 = r6.getData()     // Catch: java.lang.Exception -> L61
            java.lang.String r6 = com.studio08.xbgamestream.Helpers.FileHelper.getPath(r3, r6)     // Catch: java.lang.Exception -> L61
            r0[r5] = r6     // Catch: java.lang.Exception -> L61
            java.lang.String r6 = "Got file URI from SINGLE return intent"
            android.util.Log.e(r2, r6)     // Catch: java.lang.Exception -> L61
        L28:
            r6 = r4
            goto L58
        L2a:
            android.content.ClipData r1 = r6.getClipData()     // Catch: java.lang.Exception -> L61
            if (r1 == 0) goto L57
            java.lang.String r0 = "Got file URI from MULTIPLE return intent"
            android.util.Log.e(r2, r0)     // Catch: java.lang.Exception -> L61
            android.content.ClipData r6 = r6.getClipData()     // Catch: java.lang.Exception -> L61
            int r0 = r6.getItemCount()     // Catch: java.lang.Exception -> L61
            java.lang.String[] r0 = new java.lang.String[r0]     // Catch: java.lang.Exception -> L61
            r1 = r5
        L40:
            int r2 = r6.getItemCount()     // Catch: java.lang.Exception -> L61
            if (r1 >= r2) goto L28
            android.content.ClipData$Item r2 = r6.getItemAt(r1)     // Catch: java.lang.Exception -> L61
            android.net.Uri r2 = r2.getUri()     // Catch: java.lang.Exception -> L61
            java.lang.String r2 = com.studio08.xbgamestream.Helpers.FileHelper.getPath(r3, r2)     // Catch: java.lang.Exception -> L61
            r0[r1] = r2     // Catch: java.lang.Exception -> L61
            int r1 = r1 + 1
            goto L40
        L57:
            r6 = r5
        L58:
            com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService r1 = r3.castBindService     // Catch: java.lang.Exception -> L61
            boolean r2 = r3.AUDIO_CAST_MODE     // Catch: java.lang.Exception -> L61
            r1.setFilePaths(r0, r2)     // Catch: java.lang.Exception -> L61
            r5 = r6
            goto L65
        L61:
            r6 = move-exception
            r6.printStackTrace()
        L65:
            if (r5 != 0) goto L70
            java.lang.String r5 = "Invalid file selected. Please use a different file... If this happens on valid file its possible its in a protected directory. Try moving it to internal storage."
            android.widget.Toast r4 = android.widget.Toast.makeText(r3, r5, r4)
            r4.show()
        L70:
            r3.updateInfoText()
        L73:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.ScreenCastActivity.onActivityResult(int, int, android.content.Intent):void");
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        Log.e("HERE", "on back pressed restart main");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(131072);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onNewIntent(Intent intent) {
        Log.e("HERE", "On New Intent");
        overridePendingTransition(17432576, 17432577);
        if (intent.getBooleanExtra("showRemoteView", false)) {
            Log.e("HERE", "setup remote view");
            showRemoteView();
        } else if (intent.getBooleanExtra("showCastView", false)) {
            Log.e("HERE", "setup cast view");
            setCastType(intent);
            showCastView();
        }
        super.onNewIntent(intent);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        overridePendingTransition(17432576, 17432577);
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onDeviceReady(ConnectableDevice connectableDevice) {
        Log.e("HERE", connectableDevice.getFriendlyName());
        Toast.makeText(this, "Connected to: " + connectableDevice.getFriendlyName(), 0).show();
        mDevice = connectableDevice;
        updateInfoText();
        if (this.castServiceBound.booleanValue()) {
            this.castBindService.setDevice(connectableDevice);
        }
        this.analyticsClient.logCustomEvent("cast_xbox_discovered", connectableDevice.getFriendlyName());
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onDeviceDisconnected(ConnectableDevice connectableDevice) {
        updateInfoText();
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onConnectionFailed(ConnectableDevice connectableDevice, ServiceCommandError serviceCommandError) {
        updateInfoText();
    }

    @Override // com.connectsdk.discovery.DiscoveryManagerListener
    public void onDeviceAdded(DiscoveryManager discoveryManager, ConnectableDevice connectableDevice) {
        if (connectableDevice.getFriendlyName().contains("Xbox")) {
            Log.e("HERE", "FOUND XBOX");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.studio08.xbgamestream.ScreenCastActivity$23  reason: invalid class name */
    /* loaded from: /app/base.apk/classes3.dex */
    public static /* synthetic */ class AnonymousClass23 {
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

    protected void onSeekBarMoved(long j) {
        if (this.mMediaControl == null || !mDevice.hasCapability(MediaControl.Seek)) {
            return;
        }
        this.mMediaControl.seek(j, new ResponseListener<Object>() { // from class: com.studio08.xbgamestream.ScreenCastActivity.19
            @Override // com.connectsdk.service.capability.listeners.ResponseListener
            public void onSuccess(Object obj) {
                Log.d("HERE", "Success on Seeking");
                ScreenCastActivity.this.startUpdating();
            }

            @Override // com.connectsdk.service.capability.listeners.ErrorListener
            public void onError(ServiceCommandError serviceCommandError) {
                Log.w("Connect SDK", "Unable to seek: " + serviceCommandError.getCode());
                ScreenCastActivity.this.startUpdating();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startUpdating() {
        Timer timer = this.refreshTimer;
        if (timer != null) {
            timer.cancel();
            this.refreshTimer = null;
        }
        Timer timer2 = new Timer();
        this.refreshTimer = timer2;
        timer2.schedule(new TimerTask() { // from class: com.studio08.xbgamestream.ScreenCastActivity.20
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                Log.d("HERE", "Updating information");
                if (ScreenCastActivity.this.mMediaControl != null && ScreenCastActivity.mDevice != null && ScreenCastActivity.mDevice.hasCapability(MediaControl.Position)) {
                    ScreenCastActivity.this.mMediaControl.getPosition(ScreenCastActivity.this.positionListener);
                }
                if (ScreenCastActivity.this.mMediaControl == null || ScreenCastActivity.mDevice == null || !ScreenCastActivity.mDevice.hasCapability(MediaControl.Duration) || ScreenCastActivity.mDevice.hasCapability(MediaControl.PlayState_Subscribe) || ScreenCastActivity.this.MEDIA_DURATION > 0) {
                    return;
                }
                ScreenCastActivity.this.mMediaControl.getDuration(ScreenCastActivity.this.durationListener);
            }
        }, 0L, 1000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopUpdating() {
        Timer timer = this.refreshTimer;
        if (timer == null) {
            return;
        }
        try {
            timer.cancel();
            this.refreshTimer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startService() {
        Intent intent = new Intent(this, ConvertForegroundService.class);
        intent.putExtra("filePath", this.castBindService.getCurrentVideoFile());
        ContextCompat.startForegroundService(this, intent);
        doBindConvertService();
    }

    public void stopService() {
        try {
            this.mService.send(Message.obtain(null, 6, hashCode(), 0));
            doUnbindConvertService();
            Log.w("HERE", "Sent stop command");
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopService(new Intent(this, ConvertForegroundService.class));
    }

    public void startForegroundCastService() {
        ContextCompat.startForegroundService(this, new Intent(this, CastToConsoleForegroundService.class));
        bindService(new Intent(this, CastToConsoleForegroundService.class), this.castToConsoleForegroundServiceBindConnection, 1);
    }
}
