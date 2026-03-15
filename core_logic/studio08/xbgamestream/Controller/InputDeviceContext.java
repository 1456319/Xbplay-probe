package com.studio08.xbgamestream.Controller;

import android.os.Build;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.InputDevice;
/* loaded from: /app/base.apk/classes3.dex */
public class InputDeviceContext {
    public boolean backIsStart;
    public boolean external;
    public boolean hasJoystickAxes;
    public boolean hasMode;
    public boolean hasSelect;
    public boolean hatXAxisUsed;
    public boolean hatYAxisUsed;
    public int id;
    public boolean ignoreBack;
    public InputDevice inputDevice;
    public boolean isNonStandardDualShock4;
    public boolean isNonStandardXboxBtController;
    public boolean isServal;
    public boolean leftTriggerAxisUsed;
    public boolean modeIsSelect;
    public String name;
    public int productId;
    public boolean rightTriggerAxisUsed;
    public boolean searchIsMode;
    public boolean triggersIdleNegative;
    public boolean usesLinuxGamepadStandardFaceButtons;
    public int vendorId;
    public Vibrator vibrator;
    public VibratorManager vibratorManager;
    public int leftStickXAxis = -1;
    public int leftStickYAxis = -1;
    public int rightStickXAxis = -1;
    public int rightStickYAxis = -1;
    public int leftTriggerAxis = -1;
    public int rightTriggerAxis = -1;
    public int hatXAxis = -1;
    public int hatYAxis = -1;
    public long startDownTime = 0;
    public short inputMap = 0;
    public byte leftTrigger = 0;
    public byte rightTrigger = 0;
    public short rightStickX = 0;
    public short rightStickY = 0;
    public short leftStickX = 0;
    public short leftStickY = 0;

    /* JADX INFO: Access modifiers changed from: package-private */
    public InputDeviceContext(InputDevice inputDevice) {
        this.inputDevice = inputDevice;
        this.vendorId = inputDevice.getVendorId();
        this.productId = inputDevice.getProductId();
        setVibration();
    }

    public void setVibration() {
        if (Build.VERSION.SDK_INT >= 31 && RumbleHelper.hasDualAmplitudeControlledRumbleVibrators(this.inputDevice.getVibratorManager())) {
            this.vibratorManager = this.inputDevice.getVibratorManager();
        } else if (this.inputDevice.getVibrator().hasVibrator()) {
            this.vibrator = this.inputDevice.getVibrator();
        }
    }

    public void destroy() {
        VibratorManager vibratorManager;
        if (Build.VERSION.SDK_INT >= 31 && (vibratorManager = this.vibratorManager) != null) {
            vibratorManager.cancel();
            return;
        }
        Vibrator vibrator = this.vibrator;
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
