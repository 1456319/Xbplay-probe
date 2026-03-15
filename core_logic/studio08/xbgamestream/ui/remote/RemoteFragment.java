package com.studio08.xbgamestream.ui.remote;

import Interfaces.SmartglassEvents;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.KeyboardMovementCalculations;
import com.studio08.xbgamestream.Helpers.PopupWebview;
import com.studio08.xbgamestream.Helpers.RewardedAdLoader;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.databinding.FragmentRemoteBinding;
import com.studio08.xbgamestream.databinding.FragmentVoiceremoteBinding;
import network.BindService;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class RemoteFragment extends Fragment implements SmartglassEvents {
    private FragmentRemoteBinding binding;
    private FragmentVoiceremoteBinding bindingVoice;
    Button buildRemote;
    private Dialog dialog;
    FloatingActionButton helpButton;
    private NavController localNavController;
    Intent serviceIntent;
    Button startRemote;
    StreamWebview streamView;
    ApiClient streamingClient;
    ApiClient.StreamingClientListener buttonPressListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.1
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
            try {
                Log.e("HERE", "Nav Remote button press: " + str);
                Helper.vibrate(RemoteFragment.this.getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (str != null && str.equals("power")) {
                ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.powerOn(RemoteFragment.this.getLiveId());
                ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.powerOn(RemoteFragment.this.getLiveId());
                ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.powerOn(RemoteFragment.this.getLiveId());
                ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.powerOff();
                return;
            }
            ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.sendSystemInputCommand(Helper.convertStringButtonToByteArray(str));
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void genericMessage(String str, String str2) {
            int i;
            try {
                try {
                    i = Integer.parseInt(RemoteFragment.this.getActivity().getSharedPreferences("SettingsSharedPref", 0).getString("keyboard_speed_key", SessionDescription.SUPPORTED_SDP_VERSION));
                } catch (NumberFormatException unused) {
                    i = 0;
                }
                if (str.equals("keyboard_input")) {
                    JSONObject jSONObject = new JSONObject(str2);
                    String string = jSONObject.getString("start");
                    String string2 = jSONObject.getString(FirebaseAnalytics.Param.DESTINATION);
                    String string3 = jSONObject.getString("keyboard_type");
                    if (TextUtils.isEmpty(string3)) {
                        Toast.makeText(RemoteFragment.this.getActivity(), "Enter a keyboard type. What app are you trying to search with?", 1).show();
                        throw new Error("Invalid Char");
                    } else if (TextUtils.isEmpty(string)) {
                        Toast.makeText(RemoteFragment.this.getActivity(), "Enter a cursor start position. What key is currently highlighted on the screen?", 1).show();
                        throw new Error("Invalid Char");
                    } else if (TextUtils.isEmpty(string2)) {
                        Toast.makeText(RemoteFragment.this.getActivity(), "Invalid character selected. What character did you type? Special characters are not supported.", 1).show();
                        throw new Error("Invalid Char");
                    } else {
                        byte[][] convertPositionsToByteArray = new KeyboardMovementCalculations(string, string2, string3).convertPositionsToByteArray();
                        if (convertPositionsToByteArray == null) {
                            Toast.makeText(RemoteFragment.this.getActivity(), "Invalid character selected. Special characters are not supported.", 0).show();
                            throw new Error("Invalid Char");
                        } else {
                            ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.sendSystemInputSequence(convertPositionsToByteArray, i);
                        }
                    }
                } else if (str.equals("keyboard_button")) {
                    if (str2.equals("Backspace")) {
                        ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.sendSystemInputSequence(new byte[][]{Helper.convertStringButtonToByteArray("x")}, i);
                    } else if (str2.equals("Space")) {
                        ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.sendSystemInputSequence(new byte[][]{Helper.convertStringButtonToByteArray("y")}, i);
                    } else {
                        Log.e("HERE", "Invalid payload? " + str2);
                    }
                } else if (str.equals("show_keyboard_popup")) {
                    ((MainActivity) RemoteFragment.this.getContext()).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            new PopupWebview(RemoteFragment.this.getActivity()).showPopup(RemoteFragment.this.getView(), PopupWebview.KEYBOARD_WARNING_POPUP);
                        }
                    });
                } else {
                    Log.e("HERE", "Invalid type? " + str);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    boolean inRemoteView = false;
    private int RETRY_COUNT = 3;
    private int CONNECT_TIMEOUT = 1500;
    private ServiceConnection localServiceConnection = new ServiceConnection() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.2
        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            ((MainActivity) RemoteFragment.this.requireActivity()).mServiceBound = false;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService = ((BindService.MyBinder) iBinder).getService();
            ((MainActivity) RemoteFragment.this.requireActivity()).mServiceBound = true;
            ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.setListener(RemoteFragment.this);
            ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.discover();
        }
    };

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ConstraintLayout root;
        Log.e("HERE", "Creating frag");
        ((MainActivity) getActivity()).setOrientationPortrait();
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("nav_remote");
        getActivity().getWindow().setSoftInputMode(48);
        if (checkIfVoiceRemote()) {
            FragmentVoiceremoteBinding inflate = FragmentVoiceremoteBinding.inflate(layoutInflater, viewGroup, false);
            this.bindingVoice = inflate;
            root = inflate.getRoot();
            setupVoiceRemoteViews(root);
        } else {
            FragmentRemoteBinding inflate2 = FragmentRemoteBinding.inflate(layoutInflater, viewGroup, false);
            this.binding = inflate2;
            root = inflate2.getRoot();
            setupRemoteViews(root);
        }
        setupSharedViews(root);
        this.localNavController = ((MainActivity) requireActivity()).navController;
        return root;
    }

    private void setupVoiceRemoteViews(View view) {
        this.buildRemote = (Button) view.findViewById(R.id.remote_builder_button);
    }

    private void setupRemoteViews(View view) {
        Button button = (Button) view.findViewById(R.id.remote_builder_button);
        this.buildRemote = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                ((MainActivity) RemoteFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_builder_remote");
                ((MainActivity) RemoteFragment.this.getActivity()).setOrientationPortrait();
                RemoteFragment.this.loadWifiRemoteBuilderView();
            }
        });
        if (BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR) || BuildConfig.FLAVOR.equals("legacySdkVersion")) {
            this.buildRemote.setVisibility(0);
        }
    }

    private void setupSharedViews(View view) {
        StreamWebview streamWebview = (StreamWebview) view.findViewById(R.id.webview1);
        this.streamView = streamWebview;
        streamWebview.setBackgroundColor(0);
        this.streamView.init();
        Button button = (Button) view.findViewById(R.id.remote_button);
        this.startRemote = button;
        button.setVisibility(0);
        this.startRemote.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                ((MainActivity) RemoteFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_nav_remote");
                ((MainActivity) RemoteFragment.this.getActivity()).rewardedAd.setCallbackListener(new RewardedAdLoader.RewardAdListener() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.4.1
                    @Override // com.studio08.xbgamestream.Helpers.RewardedAdLoader.RewardAdListener
                    public void onRewardComplete() {
                        Log.e("HERE", "onRewardCompleteCaught!");
                        RemoteFragment.this.connectToConsole(RemoteFragment.this.RETRY_COUNT);
                    }
                });
                ((MainActivity) RemoteFragment.this.getActivity()).showConnectAdPossibly();
            }
        });
        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.help_button);
        this.helpButton = floatingActionButton;
        floatingActionButton.setVisibility(0);
        this.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                if (RemoteFragment.this.checkIfVoiceRemote()) {
                    new PopupWebview(RemoteFragment.this.getActivity()).showPopup(view2, PopupWebview.VOICE_REMOTE_POPUP);
                } else {
                    new PopupWebview(RemoteFragment.this.getActivity()).showPopup(view2, PopupWebview.MEDIA_REMOTE_POPUP);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkIfVoiceRemote() {
        if (NavHostFragment.findNavController(this).getCurrentDestination().getId() == R.id.nav_voiceremote) {
            Log.e("HERE", "Using Voice Remote");
            return true;
        }
        return false;
    }

    public void connectToConsole(int i) {
        try {
            Log.e("Remote", "Connecting to console retries left:" + i);
            if (getActivity() == null || !isAdded()) {
                Log.e("HERE", "Caught activity not added! lets reload the fucking activity for some reason");
                this.localNavController.navigate(R.id.nav_remote);
                ((MainActivity) getContext()).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.6
                    @Override // java.lang.Runnable
                    public void run() {
                        Toast.makeText(RemoteFragment.this.getContext(), "Disconnected. Please reconnect", 1).show();
                    }
                });
            }
            if (((MainActivity) getActivity()).mServiceBound && ((MainActivity) getActivity()).mBoundService.ready) {
                Log.e("HERE", "Detected service running. Loading wifi remote");
                loadWifiRemoteView();
                return;
            }
            try {
                setProgressDialog(getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.serviceIntent = new Intent((MainActivity) getContext(), BindService.class);
            this.serviceIntent.putExtra("notification_type", getActivity().getSharedPreferences("SettingsSharedPref", 0).getString("notification_remote_key", "close_on_exit"));
            ContextCompat.startForegroundService(getActivity(), this.serviceIntent);
            ((MainActivity) requireActivity()).mServiceConnection = this.localServiceConnection;
            ((MainActivity) getContext()).bindService(this.serviceIntent, this.localServiceConnection, 1);
            if (((MainActivity) requireActivity()).mServiceBound) {
                ((MainActivity) requireActivity()).mBoundService.discover();
            }
            retryConnect(i);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void retryConnect(final int i) {
        if (i <= 0) {
            closeProgressDialog();
            showErrorConnectDialog();
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.7
            @Override // java.lang.Runnable
            public void run() {
                RemoteFragment.this.connectToConsole(i - 1);
            }
        }, this.CONNECT_TIMEOUT);
    }

    private void showErrorConnectDialog() {
        closeProgressDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Unable to Connect");
        builder.setMessage("Unable to connect to console. Ensure you are on the same WiFi network as your console and it's powered on.\n\nTips: If you are still unable to connect, hold the power button on your console for 10 seconds to hard reboot it, then restart the app and again. This almost always fixes any issues.");
        builder.setCancelable(true);
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.8
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        if (!getLiveId().equals("")) {
            builder.setMessage("Unable to connect to console. Ensure you are on the same WiFi network as your console and it's powered on.\n\nTips: If you are still unable to connect, hold the power button on your console for 10 seconds to hard reboot it, then restart the app and again. This almost always fixes any issues.\n\nIf your Xbox is currently powered off, send the wake command to attempt to turn it on.\n\nAn Xbox can only be powered on by a wifi remote if it is in standby mode (must enable in console's settings). If you powered off your console by pressing the power button on your console (as apposed to using a controller), then standby mode is disabled and this app won't be able to turn it on.");
            builder.setPositiveButton("Send Wake Command", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.9
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.e("HERE", RemoteFragment.this.getLiveId());
                    ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.powerOn(RemoteFragment.this.getLiveId());
                    ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.powerOn(RemoteFragment.this.getLiveId());
                    ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.powerOn(RemoteFragment.this.getLiveId());
                }
            });
        }
        builder.show();
    }

    public void loadWifiRemoteView() {
        closeProgressDialog();
        if (checkIfVoiceRemote()) {
            if (Helper.checkIfAlreadyHavePermission("android.permission.RECORD_AUDIO", getActivity())) {
                Log.e("HERE", "Already have audio perm");
            } else {
                Toast.makeText(getActivity(), "Grant Audio Permissions", 0).show();
                Helper.requestForSpecificPermission(new String[]{"android.permission.RECORD_AUDIO"}, getActivity());
                return;
            }
        } else if (Build.VERSION.SDK_INT >= 33) {
            if (Helper.checkIfAlreadyHavePermission("android.permission.POST_NOTIFICATIONS", getActivity())) {
                Log.e("HERE", "Already have notification perm");
            } else {
                Toast.makeText(getActivity(), "Grant Notification Permissions", 0).show();
                Helper.requestForSpecificPermission(new String[]{"android.permission.POST_NOTIFICATIONS"}, getActivity());
                return;
            }
        }
        this.startRemote.setVisibility(4);
        this.buildRemote.setVisibility(4);
        this.helpButton.setVisibility(4);
        if (!this.inRemoteView) {
            ApiClient apiClient = new ApiClient(getActivity(), this.streamView);
            this.streamingClient = apiClient;
            apiClient.setCustomObjectListener(this.buttonPressListener);
            if (checkIfVoiceRemote()) {
                this.streamingClient.doWifiVoiceRemote();
            } else {
                this.streamingClient.doWifRemote();
            }
            this.inRemoteView = true;
            return;
        }
        Log.e("HERE", "inRemoteView = true");
    }

    public void loadWifiRemoteBuilderView() {
        closeProgressDialog();
        this.startRemote.setVisibility(4);
        this.buildRemote.setVisibility(4);
        this.helpButton.setVisibility(4);
        ApiClient apiClient = new ApiClient(getActivity(), this.streamView);
        this.streamingClient = apiClient;
        apiClient.setCustomObjectListener(this.buttonPressListener);
        this.streamingClient.doControllerBuilder("navigationRemoteControllerOption");
    }

    public void closeProgressDialog() {
        try {
            Dialog dialog = this.dialog;
            if (dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setProgressDialog(Context context) {
        Log.e("HERE", "called show progress dialog!!!!");
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setPadding(10, 10, 10, 10);
        linearLayout.setGravity(17);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.gravity = 17;
        linearLayout.setLayoutParams(layoutParams);
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, 0, 0);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(0);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams2.gravity = 17;
        TextView textView = new TextView(context);
        textView.setText("Connecting...");
        textView.setTextColor(Color.parseColor("#FFFFFF"));
        textView.setTextSize(20.0f);
        textView.setLayoutParams(layoutParams2);
        linearLayout.addView(progressBar);
        linearLayout.addView(textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(linearLayout);
        closeProgressDialog();
        AlertDialog create = builder.create();
        this.dialog = create;
        create.show();
        if (this.dialog.getWindow() != null) {
            WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams();
            layoutParams3.copyFrom(this.dialog.getWindow().getAttributes());
            layoutParams3.width = -2;
            layoutParams3.height = -2;
            this.dialog.getWindow().setAttributes(layoutParams3);
        }
    }

    @Override // Interfaces.SmartglassEvents
    public void xboxDiscovered() {
        Log.e("RemoteFrag", "Xbox Discovered");
        try {
            ((MainActivity) requireActivity()).mBoundService.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // Interfaces.SmartglassEvents
    public void xboxConnected() {
        Log.e("RemoteFrag", "Xbox Connected");
        try {
            ((MainActivity) requireActivity()).mBoundService.openChannels();
            ((MainActivity) getContext()).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.10
                @Override // java.lang.Runnable
                public void run() {
                    RemoteFragment.this.loadWifiRemoteView();
                }
            });
            saveLiveId(((MainActivity) requireActivity()).mBoundService.getLiveId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveLiveId(String str) {
        Log.e("HERE", "saving live id: " + str);
        new EncryptClient(getActivity()).saveValue("liveId", str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getLiveId() {
        return new EncryptClient(getContext()).getValue("liveId");
    }

    @Override // Interfaces.SmartglassEvents
    public void xboxDisconnected() {
        try {
            Log.e("HERE", "XBOX disconnected!!!");
            ((MainActivity) requireActivity()).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.ui.remote.RemoteFragment.11
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        ((MainActivity) RemoteFragment.this.requireActivity()).mBoundService.ready = false;
                        RemoteFragment remoteFragment = RemoteFragment.this;
                        remoteFragment.connectToConsole(remoteFragment.RETRY_COUNT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        Log.e("HERE", "onPause");
        closeProgressDialog();
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(48);
        closeProgressDialog();
        Log.e("HERE", "onResume");
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        try {
            this.streamingClient.cleanUp();
        } catch (Exception unused) {
        }
        this.binding = null;
        this.bindingVoice = null;
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        try {
            getActivity().getWindow().setSoftInputMode(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
