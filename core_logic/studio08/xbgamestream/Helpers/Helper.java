package com.studio08.xbgamestream.Helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.studio08.xbgamestream.PWAMainMenuActivity;
import constants.SystemInputButtonMappings;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Random;
import javax.jmdns.impl.constants.DNSConstants;
import org.json.JSONArray;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class Helper {
    public static void addShortcutToHomeScreen(final Context context, final String str, final String str2, final String str3, final String str4) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            new Thread(new Runnable() { // from class: com.studio08.xbgamestream.Helpers.Helper.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        IconCompat createWithBitmap = IconCompat.createWithBitmap(BitmapFactory.decodeStream(new URL(str3).openConnection().getInputStream()));
                        String str5 = "xcloudstart";
                        if (str4.equals("xhome")) {
                            str5 = "xhomestart";
                        }
                        ShortcutManagerCompat.requestPinShortcut(context, new ShortcutInfoCompat.Builder(context, str).setIntent(new Intent(context, PWAMainMenuActivity.class).setAction(str5).putExtra("titleId", str).addFlags(335544320)).setShortLabel(str2).setLongLabel(str2).setIcon(createWithBitmap).build(), null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Toast.makeText(context, "cant make shortcut", 0).show();
        }
    }

    public static void checkIfUpdateAvailable(final Context context) {
        try {
            AppUpdateManagerFactory.create(context).getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener() { // from class: com.studio08.xbgamestream.Helpers.Helper$$ExternalSyntheticLambda2
                @Override // com.google.android.gms.tasks.OnSuccessListener
                public final void onSuccess(Object obj) {
                    Helper.lambda$checkIfUpdateAvailable$0(context, (AppUpdateInfo) obj);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$checkIfUpdateAvailable$0(final Context context, AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo.updateAvailability() == 2 && (context instanceof Activity) && !((Activity) context).isFinishing()) {
            new AlertDialog.Builder(context).setTitle("Update Available").setMessage("Please update this app to the latest version and restart it. If you don't, its possible some features might not work.").setCancelable(true).setPositiveButton("Update", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.Helper.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + context.getPackageName())));
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Helpers.Helper.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(context, "Some features might not work. You have been warned :)", 1).show();
                }
            }).show();
        }
    }

    public static byte[] convertStringButtonToByteArray(String str) {
        byte[] bArr = null;
        try {
            Field[] fields = SystemInputButtonMappings.class.getFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().toLowerCase().equals(str.toLowerCase())) {
                    bArr = (byte[]) fields[i].get(bArr);
                }
            }
        } catch (Exception unused) {
        }
        return bArr;
    }

    public static String formatTime(long j) {
        int i = (int) (j / 1000);
        int i2 = i / DNSConstants.DNS_TTL;
        int i3 = i % DNSConstants.DNS_TTL;
        int i4 = i3 / 60;
        int i5 = i3 % 60;
        if (i2 > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", Integer.valueOf(i2), Integer.valueOf(i4), Integer.valueOf(i5));
        }
        return String.format(Locale.US, "%d:%02d", Integer.valueOf(i4), Integer.valueOf(i5));
    }

    public static boolean checkWifiConnected(Context context) {
        NetworkInfo[] allNetworkInfo;
        boolean z = false;
        for (NetworkInfo networkInfo : ((ConnectivityManager) context.getSystemService("connectivity")).getAllNetworkInfo()) {
            if (networkInfo.getTypeName().equalsIgnoreCase("WIFI") && networkInfo.isConnected()) {
                z = true;
            }
        }
        return z;
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress nextElement = inetAddresses.nextElement();
                    if (!nextElement.isLoopbackAddress() && (nextElement instanceof Inet4Address)) {
                        return nextElement.getHostAddress();
                    }
                }
            }
            return null;
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject getActiveCustomPhysicalGamepadMappings(Context context) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("SettingsSharedPref", 0);
            String string = sharedPreferences.getString("physical_controller_button_mappings", null);
            if (string != null && !string.equals("null")) {
                JSONArray jSONArray = new JSONArray(sharedPreferences.getString("physical_controller_configs", "[]"));
                int length = jSONArray.length();
                String str = null;
                for (int i = 0; i < length; i++) {
                    JSONObject jSONObject = jSONArray.getJSONObject(i);
                    if (jSONObject.getString("name").equals(string)) {
                        str = jSONObject.getString("data");
                    }
                }
                Toast.makeText(context, "Loaded Custom Controller Config: " + string, 1).show();
                return new JSONObject(str);
            }
            return null;
        } catch (Exception e) {
            Toast.makeText(context, "Error loading custom controller" + e.getMessage(), 1).show();
            return null;
        }
    }

    public static void vibrate(Context context) {
        try {
            if (!context.getSharedPreferences("SettingsSharedPref", 0).getBoolean("vibrate_key", true)) {
                Log.w("HERE", "ignoring vibrate due to disabled in settings");
                return;
            }
            Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(40L);
            } else {
                Log.w("Can Vibrate", "NO");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService("input_method");
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus == null) {
                currentFocus = new View(activity);
            }
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRenderEngine(Context context) {
        String string = context.getSharedPreferences("SettingsSharedPref", 0).getString("render_engine_key", "empty");
        Log.e("RenderEngine", "Current render engine: " + string);
        if (string.equals("empty")) {
            String deviceName = getDeviceName();
            Log.e("DeviceName", "Device name: " + deviceName);
            if (deviceName.contains("Odin") && !deviceName.contains("Lite")) {
                Log.e("RenderEngine", "Using geckoview");
                return "geckoview";
            } else if (deviceName.equals("Amazon_AFTKA")) {
                Log.e("RenderEngine", "Using geckoview");
                return "geckoview";
            } else {
                Log.e("RenderEngine", "Using default webview");
                return "webview";
            }
        }
        return string;
    }

    public static double getRumbleIntensity(Context context) {
        int i = context.getSharedPreferences("SettingsSharedPref", 0).getInt("rumble_intensity_key", 1);
        if (i != 0) {
            return i * 10;
        }
        return 8.0d;
    }

    public static String getDeviceName() {
        try {
            String str = Build.MANUFACTURER;
            String str2 = Build.MODEL;
            if (str2.startsWith(str)) {
                return capitalize(str2);
            }
            return capitalize(str) + "_" + str2;
        } catch (Exception unused) {
            return "";
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char charAt = str.charAt(0);
        return Character.isUpperCase(charAt) ? str : Character.toUpperCase(charAt) + str.substring(1);
    }

    public static boolean checkIfAlreadyHavePermission(String str, Context context) {
        int checkSelfPermission = ContextCompat.checkSelfPermission(context, str);
        Log.e("PERM", "Permission: " + checkSelfPermission);
        return checkSelfPermission == 0;
    }

    public static void requestForSpecificPermission(String[] strArr, Context context) {
        ActivityCompat.requestPermissions((Activity) context, strArr, 101);
    }

    public static String xorDecode(String str) {
        if (str != null && !str.isEmpty()) {
            String[] split = str.split("\\?", 2);
            String str2 = split[0];
            String str3 = split.length > 1 ? split[1] : "";
            StringBuilder sb = new StringBuilder();
            try {
                String decode = URLDecoder.decode(str2, "UTF-8");
                for (int i = 0; i < decode.length(); i++) {
                    char charAt = decode.charAt(i);
                    if (i % 2 == 1) {
                        sb.append((char) (charAt ^ 2));
                    } else {
                        sb.append(charAt);
                    }
                }
                if (!str3.isEmpty()) {
                    sb.append("?").append(str3);
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    public static void showRatingApiMaybe(final Activity activity) {
        try {
            if (new Random().nextInt(100) < 80) {
                return;
            }
            final ReviewManager create = ReviewManagerFactory.create(activity);
            create.requestReviewFlow().addOnCompleteListener(new OnCompleteListener() { // from class: com.studio08.xbgamestream.Helpers.Helper$$ExternalSyntheticLambda1
                @Override // com.google.android.gms.tasks.OnCompleteListener
                public final void onComplete(Task task) {
                    Helper.lambda$showRatingApiMaybe$2(ReviewManager.this, activity, task);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$showRatingApiMaybe$2(ReviewManager reviewManager, Activity activity, Task task) {
        if (task.isSuccessful()) {
            reviewManager.launchReviewFlow(activity, (ReviewInfo) task.getResult()).addOnCompleteListener(new OnCompleteListener() { // from class: com.studio08.xbgamestream.Helpers.Helper$$ExternalSyntheticLambda0
                @Override // com.google.android.gms.tasks.OnCompleteListener
                public final void onComplete(Task task2) {
                    Log.d(RequestConfiguration.MAX_AD_CONTENT_RATING_MA, "Finished review flow");
                }
            });
        } else {
            Log.e(RequestConfiguration.MAX_AD_CONTENT_RATING_MA, "Review flow error");
        }
    }
}
