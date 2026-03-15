package com.studio08.xbgamestream.Controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.CombinedVibration;
import android.os.IBinder;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.InputDevice;
import com.studio08.xbgamestream.Controller.LocalService;
import com.studio08.xbgamestream.Helpers.Helper;
import java.util.ArrayList;
import org.cgutman.shieldcontrollerextensions.SceManager;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class RumbleHelper {
    private Context context;
    private Vibrator deviceVibrator;
    private LocalService mService;
    private SceManager sceManager;
    ArrayList<InputDeviceContext> deviceList = new ArrayList<>();
    private boolean shouldRumbleDevice = true;
    private boolean isGCloudDevice = false;
    private ServiceConnection localLogitechServiceConnection = new ServiceConnection() { // from class: com.studio08.xbgamestream.Controller.RumbleHelper.1
        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("LocalService", "Logitech Bind service disconnected");
            RumbleHelper.this.mService = null;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("LocalService", "Logitech Bind service connected");
            RumbleHelper.this.mService = ((LocalService.LocalBinder) iBinder).getService();
        }
    };

    public RumbleHelper(Context context) {
        Log.e("Rumble", "New Rumble Helper Construction");
        this.context = context;
        init();
    }

    public void destroy() {
        Log.e("Rumble", "Cleaning up vibrator");
        for (int i = 0; i < this.deviceList.size(); i++) {
            try {
                this.deviceList.get(i).destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.deviceVibrator.cancel();
        this.sceManager.stop();
        if (!this.isGCloudDevice || this.mService == null) {
            return;
        }
        this.context.unbindService(this.localLogitechServiceConnection);
    }

    public InputDeviceContext addDevice(InputDeviceContext inputDeviceContext, InputDevice inputDevice) {
        Log.e("RumbleHelper", "Add Device" + inputDevice);
        if (Build.VERSION.SDK_INT >= 31 && hasDualAmplitudeControlledRumbleVibrators(inputDevice.getVibratorManager())) {
            inputDeviceContext.vibratorManager = inputDevice.getVibratorManager();
            this.deviceList.add(inputDeviceContext);
        } else if (inputDevice.getVibrator().hasVibrator()) {
            inputDeviceContext.vibrator = inputDevice.getVibrator();
            this.deviceList.add(inputDeviceContext);
        } else {
            Log.e("RumbleHelper", "Device has no vibration manager");
        }
        return inputDeviceContext;
    }

    private void init() {
        try {
            this.shouldRumbleDevice = this.context.getSharedPreferences("SettingsSharedPref", 0).getBoolean("rumble_device_key", true);
            SceManager sceManager = new SceManager(this.context);
            this.sceManager = sceManager;
            sceManager.start();
            this.deviceVibrator = (Vibrator) this.context.getSystemService("vibrator");
            String deviceName = Helper.getDeviceName();
            Log.e("DeviceName", "Device name: " + deviceName);
            if (deviceName.contains("Logitech_GR")) {
                this.isGCloudDevice = true;
                Log.e("LocalService", "Starting Logitech Bind");
                this.context.bindService(new Intent(this.context, LocalService.class), this.localLogitechServiceConnection, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRumble(String str) {
        Log.d("Rumble", "Starting rumble: " + str);
        try {
            JSONObject jSONObject = new JSONObject(str);
            Log.d("Rumble", jSONObject.toString());
            long j = jSONObject.getLong("duration");
            double d = jSONObject.getDouble("weakMagnitude");
            double d2 = jSONObject.getDouble("strongMagnitude");
            short min = (short) Math.min(32767.0d, d * 32767.0d * Helper.getRumbleIntensity(this.context));
            short min2 = (short) Math.min(32757.0d, d2 * 32767.0d * Helper.getRumbleIntensity(this.context));
            boolean z = false;
            for (int i = 0; i < this.deviceList.size(); i++) {
                Log.e("Rumble", "Found a connected gamepad to rumble");
                InputDeviceContext inputDeviceContext = this.deviceList.get(i);
                if (Build.VERSION.SDK_INT >= 31 && inputDeviceContext.vibratorManager != null) {
                    Log.e("Rumble", "Gamepad has dual rumble vibrator!");
                    rumbleDualVibrators(inputDeviceContext.vibratorManager, min, min2);
                } else if (this.sceManager.rumble(inputDeviceContext.inputDevice, min, min2)) {
                    Log.e("Rumble", "Gamepad is shield device!");
                } else if (inputDeviceContext.vibrator != null) {
                    Log.e("Rumble", "Gamepad only has old vibrator api");
                    rumbleSingleVibrator(inputDeviceContext.vibrator, min, min2, j);
                }
                z = true;
            }
            if (z) {
                return;
            }
            Log.i("Rumble", "Couldn't rumble gamepad, trying to rumble device: " + this.shouldRumbleDevice);
            if (this.shouldRumbleDevice) {
                rumbleSingleVibrator(this.deviceVibrator, min, min2, j);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasDualAmplitudeControlledRumbleVibrators(VibratorManager vibratorManager) {
        int[] vibratorIds = vibratorManager.getVibratorIds();
        if (vibratorIds.length != 2) {
            return false;
        }
        for (int i : vibratorIds) {
            if (!vibratorManager.getVibrator(i).hasAmplitudeControl()) {
                return false;
            }
        }
        return true;
    }

    private void rumbleDualVibrators(VibratorManager vibratorManager, short s, short s2) {
        int i = (short) ((s2 >> 8) & 255);
        int i2 = (short) ((s >> 8) & 255);
        if (i2 == 0 && i == 0) {
            vibratorManager.cancel();
            return;
        }
        int[] vibratorIds = vibratorManager.getVibratorIds();
        int[] iArr = {i, i2};
        CombinedVibration.ParallelCombination startParallel = CombinedVibration.startParallel();
        for (int i3 = 0; i3 < vibratorIds.length; i3++) {
            int i4 = iArr[i3];
            if (i4 != 0) {
                startParallel.addVibrator(vibratorIds[i3], VibrationEffect.createOneShot(60000L, i4));
            }
        }
        VibrationAttributes.Builder builder = new VibrationAttributes.Builder();
        if (Build.VERSION.SDK_INT >= 33) {
            builder.setUsage(19);
        }
        vibratorManager.vibrate(startParallel.combine(), builder.build());
    }

    private void rumbleSingleVibrator(Vibrator vibrator, short s, short s2, long j) {
        LocalService localService;
        Log.d("Rumble", "low " + ((int) s) + " high: " + ((int) s2) + " Dur: " + j);
        int min = Math.min(255, (int) ((((short) ((s >> 8) & 255)) * 0.8d) + (((short) ((s2 >> 8) & 255)) * 0.33d)));
        Log.d("Rumble", "simulatedAmplitude: " + min + "/255");
        if (min == 0) {
            vibrator.cancel();
            if (!this.isGCloudDevice || (localService = this.mService) == null) {
                return;
            }
            localService.sendPatter(0L, 0);
            return;
        }
        vibrator.cancel();
        if (Build.VERSION.SDK_INT >= 26 && vibrator.hasAmplitudeControl()) {
            VibrationEffect createOneShot = VibrationEffect.createOneShot(j, min);
            if (Build.VERSION.SDK_INT >= 33) {
                vibrator.vibrate(createOneShot, new VibrationAttributes.Builder().setUsage(19).build());
                Log.d("Rumble", "HERE6");
                return;
            }
        }
        long j2 = (long) ((min / 255.0d) * 20);
        long j3 = 20 - j2;
        if (Build.VERSION.SDK_INT >= 33) {
            vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, j2, j3}, 0), new VibrationAttributes.Builder().setUsage(19).build());
            Log.d("Rumble", "HERE4");
        } else if (this.isGCloudDevice && this.mService != null) {
            Log.d("Rumble", "HERE3 GCloud");
            this.mService.sendPatter(j, min);
        } else {
            vibrator.vibrate(new long[]{0, j2, j3}, 0, new AudioAttributes.Builder().setUsage(14).build());
            Log.d("Rumble", "HERE3");
        }
    }
}
