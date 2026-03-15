package com.studio08.xbgamestream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.sessions.settings.RemoteSettings;
import com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService;
import com.studio08.xbgamestream.Converter.ConvertForegroundService;
import com.studio08.xbgamestream.Helpers.FileHelper;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.MediaPickerHelper;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.tapjoy.TJAdUnitConstants;
import java.util.Iterator;
import java.util.List;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class PWAScreenCastClient implements ConnectableDeviceListener, DiscoveryManagerListener, CastToConsoleForegroundService.CastServiceListener, MediaPickerHelper.MediaPickerCallback {
    public static int PICKFILE_RESULT_CODE = 556;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private static ConnectableDevice mDevice;
    private static DiscoveryManager mDiscoveryManager;
    CastToConsoleForegroundService castBindService;
    Context context;
    boolean mIsBound;
    MediaControl mMediaControl;
    PlaylistControl mPlaylistControl;
    StreamWebview mSystemWebview;
    MediaPickerHelper mediaPickerHelper;
    ProgressDialog progressDialog;
    StreamWebview streamView;
    public boolean AUDIO_CAST_MODE = false;
    int VOLUME_LEVEL = 100;
    boolean isMute = false;
    Messenger mService = null;
    boolean didInit = false;
    boolean isPlaying = false;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    Boolean castServiceBound = false;
    private ServiceConnection mConnection = new ServiceConnection() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PWAScreenCastClient.this.mService = new Messenger(iBinder);
            Log.e("HERE", "Attached.");
            try {
                Message obtain = Message.obtain((Handler) null, 1);
                obtain.replyTo = PWAScreenCastClient.this.mMessenger;
                PWAScreenCastClient.this.mService.send(obtain);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.e("HERE", "Remote service connected.");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            PWAScreenCastClient.this.mService = null;
            Log.e("HERE", "Disconnected.");
        }
    };
    private ServiceConnection castToConsoleForegroundServiceBindConnection = new ServiceConnection() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.2
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("HERE", "castToConsoleForegroundServiceBindConnection Attached.");
            PWAScreenCastClient.this.castBindService = ((CastToConsoleForegroundService.MyBinder) iBinder).getService();
            PWAScreenCastClient.this.castServiceBound = true;
            if (PWAScreenCastClient.this.mMediaControl != null || PWAScreenCastClient.this.castBindService.getMediaControl() == null) {
                Log.e("HERE", "Cast not running in service yet. Unable to load media control object" + (PWAScreenCastClient.this.mMediaControl == null) + " - " + (PWAScreenCastClient.this.castBindService.getMediaControl() != null));
            } else {
                Log.e("HERE", "Hydrating media control element from service!");
                PWAScreenCastClient pWAScreenCastClient = PWAScreenCastClient.this;
                pWAScreenCastClient.mMediaControl = pWAScreenCastClient.castBindService.getMediaControl();
                PWAScreenCastClient.this.mMediaControl.subscribePlayState(PWAScreenCastClient.this.playStateListener);
            }
            if (PWAScreenCastClient.mDevice == null && PWAScreenCastClient.this.castBindService.getDevice() != null) {
                ConnectableDevice unused = PWAScreenCastClient.mDevice = PWAScreenCastClient.this.castBindService.getDevice();
            }
            PWAScreenCastClient.this.castBindService.setCastServiceListener(PWAScreenCastClient.this);
            PWAScreenCastClient.this.updateInfoText();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("HERE", "Disconnected.");
            PWAScreenCastClient.this.castServiceBound = false;
        }
    };
    ApiClient.StreamingClientListener buttonPressListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.3
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
            PWAScreenCastClient.this.sendCastRemoteCommand(str);
        }
    };
    ResponseListener castRemoteResponseListener = new ResponseListener<Object>() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.4
        @Override // com.connectsdk.service.capability.listeners.ResponseListener
        public void onSuccess(Object obj) {
        }

        @Override // com.connectsdk.service.capability.listeners.ErrorListener
        public void onError(ServiceCommandError serviceCommandError) {
            Toast.makeText(PWAScreenCastClient.this.context, "Error sending command", 0).show();
            serviceCommandError.printStackTrace();
        }
    };
    public MediaControl.PlayStateListener playStateListener = new MediaControl.PlayStateListener() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.13
        @Override // com.connectsdk.service.capability.listeners.ErrorListener
        public void onError(ServiceCommandError serviceCommandError) {
            Log.d("HERE", "Playstate Listener error = " + serviceCommandError);
            PWAScreenCastClient.this.isPlaying = false;
        }

        @Override // com.connectsdk.service.capability.listeners.ResponseListener
        public void onSuccess(MediaControl.PlayStateStatus playStateStatus) {
            Log.d("HERE", "Playstate changed | playState = " + playStateStatus);
            int i = AnonymousClass14.$SwitchMap$com$connectsdk$service$capability$MediaControl$PlayStateStatus[playStateStatus.ordinal()];
            if (i == 1) {
                if (PWAScreenCastClient.this.isPlaying) {
                    return;
                }
                PWAScreenCastClient.this.isPlaying = true;
                PWAScreenCastClient.this.updateInfoText();
            } else if (i == 2 && PWAScreenCastClient.this.isPlaying) {
                PWAScreenCastClient.this.isPlaying = false;
                PWAScreenCastClient.this.updateInfoText();
            }
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

    public void showRemoteView() {
    }

    public PWAScreenCastClient(Context context, StreamWebview streamWebview) {
        this.context = context;
        this.mSystemWebview = streamWebview;
        this.mediaPickerHelper = new MediaPickerHelper((AppCompatActivity) context, this);
    }

    @Override // com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService.CastServiceListener
    public void onMediaControlCreated(MediaControl mediaControl) {
        if (this.mMediaControl == null) {
            this.mMediaControl = mediaControl;
            mediaControl.subscribePlayState(this.playStateListener);
        }
    }

    @Override // com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService.CastServiceListener
    public void onCastTitleUpdated() {
        updateInfoText();
    }

    @Override // com.studio08.xbgamestream.Helpers.MediaPickerHelper.MediaPickerCallback
    public void onMediaPicked(List<Uri> list) {
        if (list == null || list.isEmpty()) {
            Log.d("PWAScreenCastClient", "No media selected");
            return;
        }
        Log.d("PWAScreenCastClient", "Number of items selected: " + list.size());
        Iterator<Uri> it = list.iterator();
        while (it.hasNext()) {
            Log.d("PWAScreenCastClient", "Selected URI: " + it.next().toString());
            handleFileSelectCallback(list);
        }
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
                    PWAScreenCastClient.this.updateConvertDialogStatus(string);
                } else if (i == 4) {
                    String string2 = ((JSONObject) message.obj).getString(TJAdUnitConstants.String.MESSAGE);
                    Log.e("HERE", "Got Complete Data" + string2);
                    PWAScreenCastClient.this.showConvertCompleteDialog(string2);
                } else if (i == 5) {
                    Log.e("HERE", "Screencast Activity caught video convert failure");
                    String string3 = ((JSONObject) message.obj).getString(TJAdUnitConstants.String.MESSAGE);
                    Log.e("HERE", "Got Failed Data" + string3);
                    PWAScreenCastClient.this.showConvertFailedDialog(string3);
                } else {
                    super.handleMessage(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void doBindConvertService() {
        this.context.bindService(new Intent(this.context, ConvertForegroundService.class), this.mConnection, 1);
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
            this.context.unbindService(this.mConnection);
            this.mIsBound = false;
            Log.e("HERE", "Unbinding convert service");
        }
    }

    public void cleanUp() {
        Log.e("ScreenCastActivity", "Destroyed");
        try {
            this.didInit = false;
            this.isPlaying = false;
            DiscoveryManager discoveryManager = mDiscoveryManager;
            if (discoveryManager != null) {
                discoveryManager.stop();
                mDiscoveryManager = null;
            }
            doUnbindConvertService();
            if (this.castServiceBound.booleanValue() && this.mMediaControl == null) {
                this.castBindService.stopServiceCommand();
            }
            updateInfoText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        if (this.didInit) {
            return;
        }
        DiscoveryManager.init(this.context);
        DiscoveryManager discoveryManager = DiscoveryManager.getInstance();
        mDiscoveryManager = discoveryManager;
        discoveryManager.addListener(this);
        mDiscoveryManager.start();
        startForegroundCastService();
        this.didInit = true;
    }

    public void setCastType(Intent intent) {
        if (intent == null || !intent.getBooleanExtra("audioCastType", false)) {
            Log.e("HERE", this.AUDIO_CAST_MODE + " - AUDIO_CAST_MODE");
            this.AUDIO_CAST_MODE = false;
            return;
        }
        Log.e("HERE", this.AUDIO_CAST_MODE + " - AUDIO_CAST_MODE");
        this.AUDIO_CAST_MODE = true;
    }

    void showConvertFailedDialog(String str) {
        new AlertDialog.Builder(this.context).setTitle("Failed to Convert Video").setMessage("Failed to convert video to Xbox 360 compatible format. Please report this error if there is not an obvious solution!\n\n Details: " + str).setCancelable(true).setPositiveButton("Exit", (DialogInterface.OnClickListener) null).show();
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
            new AlertDialog.Builder(this.context).setTitle("Video Converted!").setMessage("Video successfully converted to a format that is compatible with an Xbox 360!\n\nYou can view, recast, or delete this video anytime. It is stored on your device at the location of: " + str).setCancelable(true).setPositiveButton("Cast Now", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.6
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (PWAScreenCastClient.this.castBindService != null) {
                        PWAScreenCastClient.this.castBindService.setFilePaths(new String[]{str}, false);
                        PWAScreenCastClient.this.updateInfoText();
                    } else {
                        Toast.makeText(PWAScreenCastClient.this.context, "Error. Restart App", 0).show();
                    }
                    PWAScreenCastClient.this.castToConsole(false);
                }
            }).setNegativeButton(HTTP.CONN_CLOSE, new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.5
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    PWAScreenCastClient.this.stopService();
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
            this.progressDialog = new ProgressDialog(this.context);
        }
        this.progressDialog.setTitle("Converting Video to Valid Xbox 360 Format");
        this.progressDialog.setMessage(str);
        this.progressDialog.setProgressStyle(0);
        this.progressDialog.setCancelable(true);
        this.progressDialog.setButton(-1, "Run In Background", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                PWAScreenCastClient.this.progressDialog.dismiss();
            }
        });
        this.progressDialog.setButton(-2, "Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.8
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                PWAScreenCastClient.this.progressDialog.dismiss();
                PWAScreenCastClient.this.stopService();
            }
        });
        try {
            this.progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCastRemoteCommand(final String str) {
        ((Activity) this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.9
            /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:69:0x01c2 -> B:75:0x01de). Please submit an issue!!! */
            @Override // java.lang.Runnable
            public void run() {
                char c;
                PWAScreenCastClient pWAScreenCastClient;
                try {
                    if (PWAScreenCastClient.this.mMediaControl == null) {
                        return;
                    }
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
                            PWAScreenCastClient.this.mMediaControl.pause(PWAScreenCastClient.this.castRemoteResponseListener);
                            break;
                        case 1:
                            PWAScreenCastClient.this.mMediaControl.play(PWAScreenCastClient.this.castRemoteResponseListener);
                            break;
                        case 2:
                            PWAScreenCastClient.this.mMediaControl.stop(PWAScreenCastClient.this.castRemoteResponseListener);
                            break;
                        case 3:
                            PWAScreenCastClient.this.mMediaControl.rewind(PWAScreenCastClient.this.castRemoteResponseListener);
                            break;
                        case 4:
                            PWAScreenCastClient.this.mMediaControl.fastForward(PWAScreenCastClient.this.castRemoteResponseListener);
                            break;
                        case 5:
                            PWAScreenCastClient.this.castBindService.playNext();
                            break;
                        case 6:
                            PWAScreenCastClient.this.castBindService.playPrevious();
                            break;
                        case 7:
                            PWAScreenCastClient.this.isMute = !pWAScreenCastClient.isMute;
                            PWAScreenCastClient.mDevice.getVolumeControl().setMute(PWAScreenCastClient.this.isMute, new ResponseListener<Object>() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.9.1
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
                            if (PWAScreenCastClient.this.VOLUME_LEVEL >= 10) {
                                PWAScreenCastClient.this.VOLUME_LEVEL -= 10;
                                Toast.makeText(PWAScreenCastClient.this.context, PWAScreenCastClient.this.VOLUME_LEVEL + "%", 0).show();
                            }
                            PWAScreenCastClient.mDevice.getVolumeControl().setMute(false, null);
                            PWAScreenCastClient.mDevice.getVolumeControl().setVolume((float) (PWAScreenCastClient.this.VOLUME_LEVEL / 100.0d), null);
                            break;
                        case '\t':
                            if (PWAScreenCastClient.this.VOLUME_LEVEL <= 90) {
                                PWAScreenCastClient.this.VOLUME_LEVEL += 10;
                                Toast.makeText(PWAScreenCastClient.this.context, PWAScreenCastClient.this.VOLUME_LEVEL + "%", 0).show();
                            }
                            PWAScreenCastClient.mDevice.getVolumeControl().setMute(false, null);
                            PWAScreenCastClient.mDevice.getVolumeControl().setVolume((float) (PWAScreenCastClient.this.VOLUME_LEVEL / 100.0d), null);
                            break;
                        case '\n':
                            break;
                        default:
                            Toast.makeText(PWAScreenCastClient.this.context, "Button not mapped", 0).show();
                            break;
                    }
                    try {
                        Vibrator vibrator = (Vibrator) PWAScreenCastClient.this.context.getSystemService("vibrator");
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
                    Toast.makeText(PWAScreenCastClient.this.context, "Error sending command", 0).show();
                }
            }
        });
    }

    public void selectDevice() {
        new DevicePicker((Activity) this.context).getPickerDialog("Loading Consoles...", new AdapterView.OnItemClickListener() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.10
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                ConnectableDevice unused = PWAScreenCastClient.mDevice = (ConnectableDevice) adapterView.getItemAtPosition(i);
                PWAScreenCastClient.mDevice.addListener(PWAScreenCastClient.this);
                PWAScreenCastClient.mDevice.connect();
            }
        }).show();
    }

    public void selectFile() {
        ConnectableDevice connectableDevice = mDevice;
        if (connectableDevice == null || !connectableDevice.isConnected()) {
            Toast.makeText(this.context, "Select an Xbox first!", 0).show();
        } else if (!this.AUDIO_CAST_MODE) {
            this.mediaPickerHelper.launchVideoPicker();
        } else {
            if (Build.VERSION.SDK_INT >= 33) {
                if (!Helper.checkIfAlreadyHavePermission("android.permission.READ_MEDIA_VIDEO", this.context) || !Helper.checkIfAlreadyHavePermission("android.permission.READ_MEDIA_AUDIO", this.context)) {
                    try {
                        Helper.requestForSpecificPermission(new String[]{"android.permission.READ_MEDIA_VIDEO", "android.permission.READ_MEDIA_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.POST_NOTIFICATIONS"}, this.context);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } else if (!Helper.checkIfAlreadyHavePermission("android.permission.READ_EXTERNAL_STORAGE", this.context) || !Helper.checkIfAlreadyHavePermission("android.permission.WRITE_EXTERNAL_STORAGE", this.context)) {
                try {
                    Helper.requestForSpecificPermission(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, this.context);
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
            ((Activity) this.context).startActivityForResult(Intent.createChooser(intent, !this.AUDIO_CAST_MODE ? "Choose a video" : "Choose an audio file"), PICKFILE_RESULT_CODE);
        }
    }

    public void castToConsole(boolean z) {
        ConnectableDevice connectableDevice = mDevice;
        if (connectableDevice == null || !connectableDevice.isConnected()) {
            Toast.makeText(this.context, "Select a console first", 1).show();
            return;
        }
        CastToConsoleForegroundService castToConsoleForegroundService = this.castBindService;
        if (castToConsoleForegroundService == null || castToConsoleForegroundService.getFilePaths() == null) {
            Toast.makeText(this.context, "Choose a valid file first", 1).show();
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
        Toast.makeText(this.context, "Error Casting. Please restart", 0).show();
    }

    private void handle360FileConvert() {
        if (mDevice.getFriendlyName().contains("360") && this.castBindService.getFilePaths().length != 1) {
            castToConsole(false);
            Toast.makeText(this.context, "Notice: 360 video convert feature not available when selecting multiple files", 1).show();
            Toast.makeText(this.context, "If you get 'Playback Error', consider converting video format by selecting individual files.", 1).show();
        } else if (mDevice.getFriendlyName().contains("360") && this.castBindService.getFilePaths().length == 1 && !this.castBindService.getFilePaths()[0].contains("converted")) {
            String string = this.context.getSharedPreferences("SettingsSharedPref", 0).getString("video_convert_prompt_key", "ask");
            if (string.equals("ask")) {
                new AlertDialog.Builder(this.context).setTitle("Warning: Xbox 360 Detected").setMessage("Xbox 360's do not support most new video formats! You can try to cast this video, but it might show 'Playback Error' on your 360.\n\nIf this happens, this app has the ability to convert your video to a format that the Xbox 360 can play! Would you like to convert the video now?\n\nNote, converting videos is quite slow. Consider going into the settings and lowering the video conversion quality to increase the speed of the conversion process.").setCancelable(true).setPositiveButton("Convert", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.12
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PWAScreenCastClient.this.convertFileBackground();
                    }
                }).setNegativeButton("Continue without converting", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.PWAScreenCastClient.11
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PWAScreenCastClient.this.castToConsole(false);
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
        createConvertProgressDialog("Starting...");
        startService();
    }

    private void handleNotificationClick() {
        String stringExtra = ((Activity) this.context).getIntent().getStringExtra("outputPath");
        boolean booleanExtra = ((Activity) this.context).getIntent().getBooleanExtra("completed", false);
        String stringExtra2 = ((Activity) this.context).getIntent().getStringExtra("convertError");
        if (stringExtra != null && booleanExtra) {
            Log.e("HERE", "Convert completed! Showing dialog");
            showConvertCompleteDialog(stringExtra);
        } else if (stringExtra != null) {
            Log.e("HERE", "Convert in progress. Updating text");
        } else if (stringExtra2 != null) {
            showConvertFailedDialog(stringExtra2);
        }
    }

    public void updateInfoText() {
        String currentVideoFile;
        if (this.mSystemWebview == null || this.castBindService == null) {
            return;
        }
        ConnectableDevice connectableDevice = mDevice;
        String str = null;
        final String str2 = (connectableDevice == null || !connectableDevice.isConnected()) ? null : "Connected to device: " + mDevice.getFriendlyName();
        if (this.castBindService.getCurrentVideoFile() != null) {
            str = (this.isPlaying ? "Playing: " : "Selected: ") + FileHelper.getFileNameFromPath(currentVideoFile) + " (" + (this.castBindService.getCurrentPlayIndex() + 1) + RemoteSettings.FORWARD_SLASH_STRING + this.castBindService.getFilePaths().length + ")";
        }
        if (str != null) {
            str2 = str;
        } else if (str2 == null) {
            str2 = "";
        }
        ((Activity) this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.PWAScreenCastClient$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                PWAScreenCastClient.this.m345xc590df4b(str2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$updateInfoText$0$com-studio08-xbgamestream-PWAScreenCastClient  reason: not valid java name */
    public /* synthetic */ void m345xc590df4b(String str) {
        ApiClient.callJavaScript(this.mSystemWebview, "setCastData", str, Boolean.valueOf(this.isPlaying));
    }

    public void handleFileSelectCallback(List<Uri> list) {
        boolean z;
        boolean z2 = false;
        try {
            String[] strArr = new String[0];
            if (list == null || list.isEmpty()) {
                z = false;
            } else {
                Log.d("PWAScreenCastClient", "Number of items selected: " + list.size());
                strArr = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    Uri uri = list.get(i);
                    Log.d("PWAScreenCastClient", "Selected URI: " + uri.toString() + ". Is Audio: " + this.AUDIO_CAST_MODE);
                    strArr[i] = FileHelper.getPath(this.context, uri);
                }
                z = true;
            }
            this.castBindService.setFilePaths(strArr, this.AUDIO_CAST_MODE);
            z2 = z;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!z2) {
            Toast.makeText(this.context, "Invalid file selected. Please use a different file... If this happens on a valid file, it might be in a protected directory. Try moving it to internal storage.", 1).show();
        }
        updateInfoText();
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0060  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void handleFileSelectCallback(android.content.Intent r7) {
        /*
            r6 = this;
            r0 = 1
            r1 = 0
            java.lang.String[] r2 = new java.lang.String[r1]     // Catch: java.lang.Exception -> L5a
            android.net.Uri r3 = r7.getData()     // Catch: java.lang.Exception -> L5a
            java.lang.String r4 = "HERE"
            if (r3 == 0) goto L21
            java.lang.String[] r2 = new java.lang.String[r0]     // Catch: java.lang.Exception -> L5a
            android.content.Context r3 = r6.context     // Catch: java.lang.Exception -> L5a
            android.net.Uri r7 = r7.getData()     // Catch: java.lang.Exception -> L5a
            java.lang.String r7 = com.studio08.xbgamestream.Helpers.FileHelper.getPath(r3, r7)     // Catch: java.lang.Exception -> L5a
            r2[r1] = r7     // Catch: java.lang.Exception -> L5a
            java.lang.String r7 = "Got file URI from SINGLE return intent"
            android.util.Log.e(r4, r7)     // Catch: java.lang.Exception -> L5a
        L1f:
            r7 = r0
            goto L51
        L21:
            android.content.ClipData r3 = r7.getClipData()     // Catch: java.lang.Exception -> L5a
            if (r3 == 0) goto L50
            java.lang.String r2 = "Got file URI from MULTIPLE return intent"
            android.util.Log.e(r4, r2)     // Catch: java.lang.Exception -> L5a
            android.content.ClipData r7 = r7.getClipData()     // Catch: java.lang.Exception -> L5a
            int r2 = r7.getItemCount()     // Catch: java.lang.Exception -> L5a
            java.lang.String[] r2 = new java.lang.String[r2]     // Catch: java.lang.Exception -> L5a
            r3 = r1
        L37:
            int r4 = r7.getItemCount()     // Catch: java.lang.Exception -> L5a
            if (r3 >= r4) goto L1f
            android.content.ClipData$Item r4 = r7.getItemAt(r3)     // Catch: java.lang.Exception -> L5a
            android.net.Uri r4 = r4.getUri()     // Catch: java.lang.Exception -> L5a
            android.content.Context r5 = r6.context     // Catch: java.lang.Exception -> L5a
            java.lang.String r4 = com.studio08.xbgamestream.Helpers.FileHelper.getPath(r5, r4)     // Catch: java.lang.Exception -> L5a
            r2[r3] = r4     // Catch: java.lang.Exception -> L5a
            int r3 = r3 + 1
            goto L37
        L50:
            r7 = r1
        L51:
            com.studio08.xbgamestream.CastToConsole.CastToConsoleForegroundService r3 = r6.castBindService     // Catch: java.lang.Exception -> L5a
            boolean r4 = r6.AUDIO_CAST_MODE     // Catch: java.lang.Exception -> L5a
            r3.setFilePaths(r2, r4)     // Catch: java.lang.Exception -> L5a
            r1 = r7
            goto L5e
        L5a:
            r7 = move-exception
            r7.printStackTrace()
        L5e:
            if (r1 != 0) goto L6b
            android.content.Context r7 = r6.context
            java.lang.String r1 = "Invalid file selected. Please use a different file... If this happens on valid file its possible its in a protected directory. Try moving it to internal storage."
            android.widget.Toast r7 = android.widget.Toast.makeText(r7, r1, r0)
            r7.show()
        L6b:
            r6.updateInfoText()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.PWAScreenCastClient.handleFileSelectCallback(android.content.Intent):void");
    }

    @Override // com.connectsdk.device.ConnectableDeviceListener
    public void onDeviceReady(ConnectableDevice connectableDevice) {
        Log.e("HERE", connectableDevice.getFriendlyName());
        Toast.makeText(this.context, "Connected to: " + connectableDevice.getFriendlyName(), 0).show();
        mDevice = connectableDevice;
        updateInfoText();
        if (this.castServiceBound.booleanValue()) {
            this.castBindService.setDevice(connectableDevice);
        }
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
    /* renamed from: com.studio08.xbgamestream.PWAScreenCastClient$14  reason: invalid class name */
    /* loaded from: /app/base.apk/classes3.dex */
    public static /* synthetic */ class AnonymousClass14 {
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

    public void startService() {
        Intent intent = new Intent(this.context, ConvertForegroundService.class);
        intent.putExtra("filePath", this.castBindService.getCurrentVideoFile());
        ContextCompat.startForegroundService(this.context, intent);
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
        this.context.stopService(new Intent(this.context, ConvertForegroundService.class));
    }

    public void startForegroundCastService() {
        ContextCompat.startForegroundService(this.context, new Intent(this.context, CastToConsoleForegroundService.class));
        this.context.bindService(new Intent(this.context, CastToConsoleForegroundService.class), this.castToConsoleForegroundServiceBindConnection, 1);
    }
}
