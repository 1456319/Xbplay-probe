package com.studio08.xbgamestream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.images.WebImage;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironsource.mediationsdk.IronSource;
import com.studio08.xbgamestream.Controller.ControllerHandler;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.FirebaseAnalyticsClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.RewardedAdLoader;
import com.studio08.xbgamestream.Helpers.SettingsActivity;
import com.studio08.xbgamestream.Helpers.TutorialActivity;
import com.studio08.xbgamestream.Servers.Server;
import com.studio08.xbgamestream.Timers.PCheckInterface;
import com.studio08.xbgamestream.Timers.PurchaseChecker;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.databinding.ActivityMainBinding;
import com.tapjoy.TJAdUnitConstants;
import java.io.IOException;
import network.BindService;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class MainActivity extends AppCompatActivity {
    public FirebaseAnalyticsClient analyticsClient;
    private ActivityMainBinding binding;
    public ControllerHandler controllerHandler;
    public DrawerLayout drawer;
    private AppBarConfiguration mAppBarConfiguration;
    public BindService mBoundService;
    CastContext mCastContext;
    public CastSession mCastSession;
    public ServiceConnection mServiceConnection;
    private SessionManager mSessionManager;
    private StreamWebview mainWebView;
    private MenuItem mediaRouteMenuItem;
    public NavController navController;
    IntroductoryOverlay overlay;
    public RewardedAdLoader rewardedAd;
    public Server server;
    private Dialog showTrialDialog;
    private MenuItem unlockMenuItem;
    public boolean inFullScreenMode = false;
    public String consoleText = null;
    public String mirrorcastText = null;
    public boolean mServiceBound = false;
    private SessionManagerListener<CastSession> mSessionManagerListener = new SessionManagerListenerImpl();
    public String tvCodeUri = "";
    public String xCloudShortcutTitleId = null;
    private PurchaseChecker purchaseChecker = new PurchaseChecker(this, new PCheckInterface() { // from class: com.studio08.xbgamestream.MainActivity.1
        @Override // com.studio08.xbgamestream.Timers.PCheckInterface
        public void PCheckTriggered() {
            try {
                if (MainActivity.this.rewardedAd == null || !RewardedAdLoader.shouldShowAd(MainActivity.this)) {
                    return;
                }
                if (MainActivity.this.navController.getCurrentDestination() != null) {
                    int id = MainActivity.this.navController.getCurrentDestination().getId();
                    MainActivity.this.navController.popBackStack(id, true);
                    MainActivity.this.navController.navigate(id);
                }
                if (MainActivity.this.showTrialDialog != null && MainActivity.this.showTrialDialog.isShowing()) {
                    MainActivity.this.showTrialDialog.dismiss();
                }
                MainActivity.this.showTrialDialog = new AlertDialog.Builder(MainActivity.this).setTitle("Free Trial Ended").setMessage("Thanks for trying out the app! Please purchase the full version to continue use.").setCancelable(false).setPositiveButton("Upgrade", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.1.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("HERE", "Upgrade");
                        if (MainActivity.this.rewardedAd != null) {
                            MainActivity.this.rewardedAd.buyAdRemoval();
                        }
                    }
                }).setNegativeButton("Exit App", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.1.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finishAffinity();
                    }
                }).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    private CastStateListener mCastStateListener = new AnonymousClass2();

    /* loaded from: /app/base.apk/classes3.dex */
    private class SessionManagerListenerImpl implements SessionManagerListener<CastSession> {
        private SessionManagerListenerImpl() {
        }

        @Override // com.google.android.gms.cast.framework.SessionManagerListener
        public void onSessionStarted(CastSession castSession, String str) {
            Log.e("HERE", "onSessionStarted");
            MainActivity.this.invalidateOptionsMenu();
            MainActivity.this.mCastSession = castSession;
            MainActivity.this.listenForCastMessages();
            MainActivity.this.startCastSession();
        }

        @Override // com.google.android.gms.cast.framework.SessionManagerListener
        public void onSessionStarting(CastSession castSession) {
            Log.e("HERE", "onSessionStarting");
            try {
                new AlertDialog.Builder(MainActivity.this).setTitle("Cast Warning").setMessage("The settings that you have in the app will be applied to your cast device. Consider setting the client to 'Android' in the settings if there is lag on your TV. \n\nThis feature is still in development! You must have a high end Android TV or Chromecast device (60FPS support and 2GB of RAM or more). This will not work with a standard Chromecast (yet - hopefully coming soon).\n\nThis is tested and working with a 'Chromecast with Google TV' device.\nPlease be patient while I get this working with non 60FPS devices :)").setCancelable(true).setPositiveButton("I Understand", (DialogInterface.OnClickListener) null).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override // com.google.android.gms.cast.framework.SessionManagerListener
        public void onSessionSuspended(CastSession castSession, int i) {
            Log.e("HERE", "onSessionSuspended");
            MainActivity.this.mCastSession = castSession;
        }

        @Override // com.google.android.gms.cast.framework.SessionManagerListener
        public void onSessionResumed(CastSession castSession, boolean z) {
            MainActivity.this.invalidateOptionsMenu();
            Log.e("HERE", "onSessionResumed");
            MainActivity.this.mCastSession = castSession;
        }

        @Override // com.google.android.gms.cast.framework.SessionManagerListener
        public void onSessionResuming(CastSession castSession, String str) {
            Log.e("HERE", "onSessionResuming");
            MainActivity.this.mCastSession = castSession;
        }

        @Override // com.google.android.gms.cast.framework.SessionManagerListener
        public void onSessionStartFailed(CastSession castSession, int i) {
            Log.e("HERE", "onSessionStartFailed");
            MainActivity.this.mCastSession = castSession;
            try {
                new AlertDialog.Builder(MainActivity.this).setTitle("Failed to Cast").setMessage("Your cast devices refused the connection. Try restarting it").setCancelable(true).setPositiveButton("OK", (DialogInterface.OnClickListener) null).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override // com.google.android.gms.cast.framework.SessionManagerListener
        public void onSessionEnded(CastSession castSession, int i) {
            Log.e("HERE", "onSessionEnded");
            MainActivity.this.mCastSession = castSession;
        }

        @Override // com.google.android.gms.cast.framework.SessionManagerListener
        public void onSessionEnding(CastSession castSession) {
            Log.e("HERE", "onSessionEnding");
            MainActivity.this.mCastSession = castSession;
        }

        @Override // com.google.android.gms.cast.framework.SessionManagerListener
        public void onSessionResumeFailed(CastSession castSession, int i) {
            Log.e("HERE", "onSessionResumeFailed");
            MainActivity.this.mCastSession = castSession;
        }
    }

    /* renamed from: com.studio08.xbgamestream.MainActivity$2  reason: invalid class name */
    /* loaded from: /app/base.apk/classes3.dex */
    class AnonymousClass2 implements CastStateListener {
        AnonymousClass2() {
        }

        @Override // com.google.android.gms.cast.framework.CastStateListener
        public void onCastStateChanged(int i) {
            if (i != 1) {
                try {
                    if (MainActivity.this.inFullScreenMode) {
                        return;
                    }
                    MainActivity.this.runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.MainActivity.2.1
                        @Override // java.lang.Runnable
                        public void run() {
                            MainActivity.this.overlay = new IntroductoryOverlay.Builder(MainActivity.this, MainActivity.this.mediaRouteMenuItem).setTitleText("Cast your Xbox One or Series X/S screen straight to your TV. Now you can leave your Xbox in the living room and play from the smart TV in your bedroom!").setSingleTime().setOnOverlayDismissedListener(new IntroductoryOverlay.OnOverlayDismissedListener() { // from class: com.studio08.xbgamestream.MainActivity.2.1.1
                                @Override // com.google.android.gms.cast.framework.IntroductoryOverlay.OnOverlayDismissedListener
                                public void onOverlayDismissed() {
                                    MainActivity.this.overlay = null;
                                }
                            }).build();
                            MainActivity.this.overlay.show();
                        }
                    });
                } catch (Error e) {
                    e.printStackTrace();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public void updateMenu() {
        try {
            invalidateOptionsMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // android.app.Activity
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = this.unlockMenuItem;
        if (menuItem != null) {
            menuItem.setVisible(false);
            this.unlockMenuItem.setVisible(!RewardedAdLoader.getPurchaseItem(getApplicationContext()));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.unlockMenuItem = menu.findItem(R.id.action_full_version);
        if (BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR) || BuildConfig.FLAVOR.equals("legacy")) {
            try {
                this.mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        try {
            this.purchaseChecker.start();
        } catch (Exception e) {
            Log.e("Error", "Error starting purchase checker");
            e.printStackTrace();
        }
        try {
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == 0) {
                this.mCastSession = this.mSessionManager.getCurrentCastSession();
                this.mSessionManager.addSessionManagerListener(this.mSessionManagerListener, CastSession.class);
                this.mCastContext.addCastStateListener(this.mCastStateListener);
            }
        } catch (Error e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        IronSource.onResume(this);
        RewardedAdLoader rewardedAdLoader = this.rewardedAd;
        if (rewardedAdLoader != null) {
            rewardedAdLoader.resume();
        }
        updateMenu();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        try {
            this.purchaseChecker.stop();
        } catch (Exception e) {
            Log.e("Error", "Error stopping purchase checker");
            e.printStackTrace();
        }
        try {
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == 0) {
                this.mSessionManager.removeSessionManagerListener(this.mSessionManagerListener, CastSession.class);
                this.mCastContext.removeCastStateListener(this.mCastStateListener);
                this.mCastSession = null;
            }
        } catch (Error e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        IronSource.onPause(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = getSharedPreferences("rate", 0);
        int i = sharedPreferences.getInt("appOpens", 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt("appOpens", i + 1);
        edit.apply();
        Log.w("HERE", "Open counter:" + i + 1);
        unbindRemoteControllerService();
    }

    public void unbindRemoteControllerService() {
        this.binding = null;
        try {
            unbindService(this.mServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void listenForCastMessages() {
        try {
            this.mCastSession.setMessageReceivedCallbacks("urn:x-cast:xbplay-toAndroid", new Cast.MessageReceivedCallback() { // from class: com.studio08.xbgamestream.MainActivity.3
                @Override // com.google.android.gms.cast.Cast.MessageReceivedCallback
                public void onMessageReceived(CastDevice castDevice, String str, String str2) {
                    try {
                        String replace = str2.replace("\\\"", "'");
                        Log.e("HERE", "'" + replace + "' - message received from cast device here");
                        JSONObject jSONObject = new JSONObject(replace.substring(1, replace.length() - 1));
                        if (jSONObject.getString("type").equals("Error") && jSONObject.getString(TJAdUnitConstants.String.MESSAGE).equals("expired_tokens")) {
                            new AlertDialog.Builder(MainActivity.this).setTitle("Cast - Login Required").setMessage("The cast device cannot connect to your console. Please navigate to the 'consoles' tab and click connect. Then recast").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.3.1
                                @Override // android.content.DialogInterface.OnClickListener
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MainActivity.this.drawer.open();
                                }
                            }).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void startCastSession() {
        Log.e("HERE", "starting cast session");
        MediaMetadata mediaMetadata = new MediaMetadata(0);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, "xbPlay");
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, "Streaming console directly to TV");
        mediaMetadata.addImage(new WebImage(Uri.parse("https://d1o4538xtdh4nm.cloudfront.net/feature-art.png")));
        MediaInfo build = new MediaInfo.Builder("none").setStreamType(2).setMetadata(mediaMetadata).setStreamDuration(5L).build();
        RemoteMediaClient remoteMediaClient = this.mCastSession.getRemoteMediaClient();
        remoteMediaClient.load(new MediaLoadRequestData.Builder().setMediaInfo(build).build());
        remoteMediaClient.play();
        EncryptClient encryptClient = new EncryptClient(this);
        String value = encryptClient.getValue("serverId");
        String value2 = encryptClient.getValue("gsToken");
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsSharedPref", 0);
        Boolean valueOf = Boolean.valueOf(sharedPreferences.getBoolean("info_cast_key", false));
        String string = sharedPreferences.getString("video_fit_key", "cover");
        String string2 = sharedPreferences.getString("emulate_client_key", "windows");
        String string3 = sharedPreferences.getString("controller_refresh_key", "32");
        String string4 = sharedPreferences.getString("max_bitrate_key", "");
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("gsToken", value2);
            jSONObject.put("serverId", value);
            jSONObject.put(TJAdUnitConstants.String.VIDEO_INFO, valueOf);
            jSONObject.put("video-fit", string);
            jSONObject.put("gamepadRefreshRateMs", string3);
            jSONObject.put("userAgentType", string2);
            jSONObject.put("maxBitrate", string4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mCastSession.sendMessage("urn:x-cast:xbplay-config", jSONObject.toString());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        AppCompatDelegate.setDefaultNightMode(2);
        getWindow().addFlags(128);
        requestWindowFeature(1);
        if (!getSharedPreferences("SettingsSharedPref", 0).getBoolean("use_notch_key", true)) {
            setTheme(R.style.Theme_MyApplication3);
        }
        super.onCreate(bundle);
        setupGoogleAnalytics();
        try {
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == 0) {
                this.mCastContext = CastContext.getSharedInstance(this);
                this.mSessionManager = CastContext.getSharedInstance(this).getSessionManager();
                this.mCastContext.addCastStateListener(this.mCastStateListener);
            }
        } catch (Error e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        setupInitialOrientation();
        ActivityMainBinding inflate = ActivityMainBinding.inflate(getLayoutInflater());
        this.binding = inflate;
        setContentView(inflate.getRoot());
        setSupportActionBar(this.binding.appBarMain.toolbar);
        this.binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.hideSystemUI();
                Snackbar.make(view, "Fullscreen mode enabled. Press the back button to exit.", 0).setAction("Action", (View.OnClickListener) null).show();
            }
        });
        this.drawer = this.binding.drawerLayout;
        NavigationView navigationView = this.binding.navView;
        this.mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_remote, R.id.nav_home, R.id.nav_gamestream, R.id.nav_stream, R.id.nav_controller, R.id.nav_controller_builder, R.id.nav_mirrorcast, R.id.nav_filecast, R.id.nav_cast_remote, R.id.nav_tvcast, R.id.nav_audiocast, R.id.nav_more_features, R.id.nav_android_tv, R.id.nav_xcloud, R.id.nav_widgets, R.id.nav_voiceremote).setOpenableLayout(this.drawer).build();
        NavController findNavController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        this.navController = findNavController;
        NavigationUI.setupActionBarWithNavController(this, findNavController, this.mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, this.navController);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() { // from class: com.studio08.xbgamestream.MainActivity.5
            @Override // com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.nav_settings) {
                    MainActivity.this.drawer.close();
                    MainActivity.this.startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    return false;
                }
                MainActivity.this.navController.navigate(menuItem.getItemId());
                MainActivity.this.drawer.close();
                MainActivity.this.xCloudShortcutTitleId = null;
                return true;
            }
        });
        this.rewardedAd = new RewardedAdLoader(this);
        if (bundle == null) {
            handleTutorial();
            handleWifiPrompt();
            handleRateScreens();
            this.drawer.open();
        }
        handleTvCastRemoteRedirect();
        handleWidgetAdRedirect();
        Helper.checkIfUpdateAvailable(this);
        this.controllerHandler = new ControllerHandler(getApplicationContext());
        handleDeepLinkRedirect();
        handleXCloudShortcutStart();
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        NavController navController = this.navController;
        if (navController == null || navController.getCurrentDestination() == null || this.navController.getCurrentDestination().getId() != R.id.nav_controller_builder) {
            recreate();
        }
    }

    public void handleXCloudShortcutStart() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String action = getIntent().getAction();
        String stringExtra = intent.getStringExtra("titleId");
        if (TextUtils.isEmpty(stringExtra) || action == null || TextUtils.isEmpty(action) || !action.equals("xcloudstart")) {
            return;
        }
        this.xCloudShortcutTitleId = stringExtra;
        Toast.makeText(this, "Click start to play: " + this.xCloudShortcutTitleId, 0).show();
        setOrientationLandscape();
        this.drawer.close();
        this.navController.navigate(R.id.nav_xcloud);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x006f, code lost:
        if (r4.equals("xcloud") == false) goto L7;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void handleDeepLinkRedirect() {
        /*
            r8 = this;
            android.content.Intent r0 = r8.getIntent()
            android.net.Uri r0 = r0.getData()
            if (r0 == 0) goto Lda
            java.util.List r1 = r0.getPathSegments()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r3 = "Params: "
            r2.<init>(r3)
            java.lang.StringBuilder r2 = r2.append(r1)
            java.lang.StringBuilder r2 = r2.append(r0)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "LaunchIntent"
            android.util.Log.e(r3, r2)
            java.lang.String r0 = r0.getHost()
            java.lang.String r2 = "launch"
            boolean r0 = r0.equals(r2)
            r2 = 1
            if (r0 == 0) goto Lc9
            r0 = 0
            java.lang.Object r4 = r1.get(r0)
            java.lang.String r4 = (java.lang.String) r4
            r4.hashCode()
            int r5 = r4.hashCode()
            r6 = 2
            r7 = -1
            switch(r5) {
                case -764712771: goto L69;
                case 3714: goto L5e;
                case 114030935: goto L53;
                case 637428636: goto L48;
                default: goto L46;
            }
        L46:
            r0 = r7
            goto L72
        L48:
            java.lang.String r0 = "controller"
            boolean r0 = r4.equals(r0)
            if (r0 != 0) goto L51
            goto L46
        L51:
            r0 = 3
            goto L72
        L53:
            java.lang.String r0 = "xhome"
            boolean r0 = r4.equals(r0)
            if (r0 != 0) goto L5c
            goto L46
        L5c:
            r0 = r6
            goto L72
        L5e:
            java.lang.String r0 = "tv"
            boolean r0 = r4.equals(r0)
            if (r0 != 0) goto L67
            goto L46
        L67:
            r0 = r2
            goto L72
        L69:
            java.lang.String r5 = "xcloud"
            boolean r5 = r4.equals(r5)
            if (r5 != 0) goto L72
            goto L46
        L72:
            switch(r0) {
                case 0: goto Lbd;
                case 1: goto La2;
                case 2: goto L95;
                case 3: goto L88;
                default: goto L75;
            }
        L75:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r5 = "Unknown launch activity: "
            r0.<init>(r5)
            java.lang.StringBuilder r0 = r0.append(r4)
            java.lang.String r0 = r0.toString()
            android.util.Log.e(r3, r0)
            goto Lc9
        L88:
            androidx.navigation.NavController r0 = r8.navController
            int r3 = com.studio08.xbgamestream.R.id.nav_controller
            r0.navigate(r3)
            androidx.drawerlayout.widget.DrawerLayout r0 = r8.drawer
            r0.close()
            goto Lc9
        L95:
            androidx.navigation.NavController r0 = r8.navController
            int r3 = com.studio08.xbgamestream.R.id.nav_gamestream
            r0.navigate(r3)
            androidx.drawerlayout.widget.DrawerLayout r0 = r8.drawer
            r0.close()
            goto Lc9
        La2:
            int r0 = r1.size()
            if (r0 < r6) goto Lb7
            java.lang.Object r0 = r1.get(r2)
            java.lang.String r0 = (java.lang.String) r0
            r8.tvCodeUri = r0
            androidx.navigation.NavController r0 = r8.navController
            int r3 = com.studio08.xbgamestream.R.id.nav_android_tv
            r0.navigate(r3)
        Lb7:
            androidx.drawerlayout.widget.DrawerLayout r0 = r8.drawer
            r0.close()
            goto Lc9
        Lbd:
            androidx.navigation.NavController r0 = r8.navController
            int r3 = com.studio08.xbgamestream.R.id.nav_xcloud
            r0.navigate(r3)
            androidx.drawerlayout.widget.DrawerLayout r0 = r8.drawer
            r0.close()
        Lc9:
            int r0 = r1.size()
            if (r0 == 0) goto Lda
            int r0 = r1.size()
            int r0 = r0 - r2
            java.lang.Object r0 = r1.get(r0)
            java.lang.String r0 = (java.lang.String) r0
        Lda:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.MainActivity.handleDeepLinkRedirect():void");
    }

    public void showConnectAdPossibly() {
        try {
            this.rewardedAd.showRewardedAdDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getTotalNumberOfAppOpens() {
        int i = getSharedPreferences("rate", 0).getInt("appOpens", 1);
        return i >= 1000000 ? i - 1000000 : i;
    }

    private void setupGoogleAnalytics() {
        try {
            this.analyticsClient = new FirebaseAnalyticsClient(FirebaseAnalytics.getInstance(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTvCastRemoteRedirect() {
        if (getIntent().getBooleanExtra("showCastRemote", false)) {
            this.navController.navigate(R.id.nav_tvcast);
            this.drawer.close();
        }
    }

    private void handleWidgetAdRedirect() {
        if (getIntent().getBooleanExtra("widgetShowAd", false)) {
            this.navController.navigate(R.id.nav_remote);
            this.drawer.close();
            showConnectAdPossibly();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onNewIntent(Intent intent) {
        Log.e("HERE", "New Intent");
        handleTvCastRemoteRedirect();
        handleWidgetAdRedirect();
        overridePendingTransition(17432576, 17432577);
        super.onNewIntent(intent);
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        DrawerLayout drawerLayout;
        DrawerLayout drawerLayout2 = this.drawer;
        boolean z = drawerLayout2 != null && drawerLayout2.isDrawerOpen(8388611);
        if (this.inFullScreenMode) {
            showSystemUI();
        } else if (!z && (drawerLayout = this.drawer) != null) {
            drawerLayout.open();
        } else {
            new AlertDialog.Builder(this).setTitle("Exit").setMessage("Are you sure you want to exit?").setCancelable(true).setPositiveButton("Exit App", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.7
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.finishAffinity();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.6
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 444 && i2 == -1) {
            try {
                String str = "Connected to console: " + intent.getStringExtra("serverId");
                this.consoleText = str;
                ((TextView) findViewById(R.id.text_home)).setText(str);
                ((Button) findViewById(R.id.connect_button)).setText("Reload Consoles");
                this.drawer.open();
                Toast.makeText(this, "Login success! Open a streaming/controller tab!", 1).show();
            } catch (Exception unused) {
            }
        }
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_reddit) {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://www.reddit.com/r/xbPlay/")));
            } catch (ActivityNotFoundException unused) {
                Toast.makeText(this, "Error opening reddit link", 0).show();
            }
            return true;
        } else if (menuItem.getItemId() == R.id.action_discord) {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://discord.gg/zxEBXxWWza")));
            } catch (ActivityNotFoundException unused2) {
                Toast.makeText(this, "Error opening discord link", 0).show();
            }
            return true;
        } else if (menuItem.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (menuItem.getItemId() == R.id.action_rate) {
            showRatingDialog();
            return true;
        } else if (menuItem.getItemId() == R.id.action_full_version) {
            try {
                RewardedAdLoader rewardedAdLoader = this.rewardedAd;
                if (rewardedAdLoader != null) {
                    rewardedAdLoader.buyAdRemoval();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    public void setupInitialOrientation() {
        String string = getSharedPreferences("SettingsSharedPref", 0).getString("orientation_key", "auto");
        if (string.equals(TJAdUnitConstants.String.PORTRAIT)) {
            setRequestedOrientation(1);
        } else if (string.equals(TJAdUnitConstants.String.LANDSCAPE)) {
            setRequestedOrientation(0);
        } else if (string.equals("reverse_landscape")) {
            setRequestedOrientation(8);
        } else if (string.equals("full_sensor")) {
            setRequestedOrientation(10);
        }
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

    public void handleRateScreens() {
        int i = getSharedPreferences("rate", 0).getInt("appOpens", 1);
        boolean booleanExtra = getIntent().getBooleanExtra("widgetShowAd", false);
        if (i >= 1000000 || booleanExtra) {
            return;
        }
        if (i % 3 == 0) {
            showRatingDialog();
        } else if (i % 5 == 0) {
            Helper.showRatingApiMaybe(this);
        }
    }

    public void showRatingDialog() {
        this.analyticsClient.logButtonClickEvent("show_rate_app");
        new AlertDialog.Builder(this).setTitle("Rate App").setMessage("Please help support this app by leaving a rating on Google Play! Comment with any feature requests or improvements and I will do my best to get to them!").setCancelable(true).setPositiveButton("Rate", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.9
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.analyticsClient.logButtonClickEvent("open_rate_app");
                MainActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + MainActivity.this.getPackageName())));
                SharedPreferences.Editor edit = MainActivity.this.getSharedPreferences("rate", 0).edit();
                edit.putInt("appOpens", 1000000);
                edit.apply();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.8
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.analyticsClient.logButtonClickEvent("cancel_rate_app");
                Toast.makeText(MainActivity.this, "Okay :(", 1).show();
            }
        }).show();
    }

    public void handleWifiPrompt() {
        if (Helper.checkWifiConnected(getApplicationContext())) {
            return;
        }
        new AlertDialog.Builder(this).setTitle("Connect to Wifi").setMessage("You must be connected to the same Wifi network as your console. Connect now?").setCancelable(true).setPositiveButton("Connect Wifi", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.11
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
            }
        }).setNegativeButton("Continue Anyway", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.MainActivity.10
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "You might not be able to connect to your console :(", 1).show();
            }
        }).show();
    }

    public void handleTutorial() {
        String value = new EncryptClient(this).getValue("tutorialShown");
        if (TextUtils.isEmpty(value) || !TextUtils.equals(value, "1")) {
            startActivity(new Intent(this, TutorialActivity.class));
        }
    }

    public void hideSystemUI() {
        this.inFullScreenMode = true;
        this.binding.appBarMain.fab.setVisibility(4);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT < 30) {
            getWindow().getDecorView().setSystemUiVisibility(5894);
        } else {
            WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
            if (windowInsetsController == null) {
                return;
            }
            windowInsetsController.setSystemBarsBehavior(2);
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            if (getSharedPreferences("SettingsSharedPref", 0).getBoolean("use_notch_key", true)) {
                getWindow().setFlags(1024, 1024);
                getWindow().setFlags(512, 512);
                getWindow().getAttributes().layoutInDisplayCutoutMode = 1;
            }
        }
        try {
            DrawerLayout drawerLayout = this.drawer;
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSystemUI() {
        this.binding.appBarMain.fab.setVisibility(0);
        this.inFullScreenMode = false;
        getSupportActionBar().show();
        if (Build.VERSION.SDK_INT < 30) {
            getWindow().getDecorView().setSystemUiVisibility(256);
        } else {
            if (getSharedPreferences("SettingsSharedPref", 0).getBoolean("use_notch_key", true)) {
                getWindow().addFlags(2048);
                getWindow().clearFlags(1024);
                getWindow().clearFlags(512);
            }
            WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
            if (windowInsetsController == null) {
                return;
            }
            windowInsetsController.setSystemBarsBehavior(2);
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
        }
        try {
            DrawerLayout drawerLayout = this.drawer;
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // androidx.appcompat.app.AppCompatActivity
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.nav_host_fragment_content_main), this.mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
