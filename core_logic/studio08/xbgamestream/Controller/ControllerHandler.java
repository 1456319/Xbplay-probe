package com.studio08.xbgamestream.Controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.input.InputManager;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.view.InputDeviceCompat;
import androidx.mediarouter.media.MediaRouter;
import com.google.android.gms.cast.MediaError;
import java.lang.reflect.InvocationTargetException;
import kotlin.jvm.internal.ShortCompanionObject;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.crypto.tls.CipherSuite;
/* loaded from: /app/base.apk/classes3.dex */
public class ControllerHandler implements InputManager.InputDeviceListener, View.OnKeyListener, View.OnGenericMotionListener {
    private Context context;
    String currentControllerName;
    private boolean hasGameController;
    ControllerHandlerListener listener;
    SharedPreferences prefs;
    RumbleHelper rumbleHelper;
    private View sourceView;
    private final SparseArray<InputDeviceContext> inputDeviceContexts = new SparseArray<>();
    private final Vector2d inputVector = new Vector2d();
    String previousPayload = "";

    /* loaded from: /app/base.apk/classes3.dex */
    public interface ControllerHandlerListener {
        void controllerData(JSONObject jSONObject);
    }

    private float normalizeByte(byte b) {
        return b / 127.0f;
    }

    private float normalizeShort(short s) {
        return s / 32767.0f;
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        int action = keyEvent.getAction();
        if (action != 0) {
            if (action != 1) {
                return false;
            }
            return handleButtonUp(keyEvent);
        }
        return handleButtonDown(keyEvent);
    }

    @Override // android.view.View.OnGenericMotionListener
    public boolean onGenericMotion(View view, MotionEvent motionEvent) {
        return handleMotionEvent(motionEvent);
    }

    public ControllerHandler(Context context) {
        this.context = context;
        this.rumbleHelper = new RumbleHelper(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("SettingsSharedPref", 0);
        this.prefs = sharedPreferences;
        this.currentControllerName = sharedPreferences.getString("physical_controller_button_mappings", "");
    }

    public void destroy() {
        this.rumbleHelper.destroy();
    }

    public void handleRumble(String str) {
        this.rumbleHelper.handleRumble(str);
    }

    public void setListener(ControllerHandlerListener controllerHandlerListener) {
        this.listener = controllerHandlerListener;
    }

    public void setSourceView(View view) {
        this.sourceView = view;
        view.setOnKeyListener(this);
        this.sourceView.setOnGenericMotionListener(this);
    }

    public void setPassthroughView(View view) {
        this.sourceView = view;
        view.setOnKeyListener(new View.OnKeyListener() { // from class: com.studio08.xbgamestream.Controller.ControllerHandler.1
            @Override // android.view.View.OnKeyListener
            public boolean onKey(View view2, int i, KeyEvent keyEvent) {
                ControllerHandler.this.getContextForEvent(keyEvent);
                return false;
            }
        });
        this.sourceView.setOnGenericMotionListener(new View.OnGenericMotionListener() { // from class: com.studio08.xbgamestream.Controller.ControllerHandler.2
            @Override // android.view.View.OnGenericMotionListener
            public boolean onGenericMotion(View view2, MotionEvent motionEvent) {
                ControllerHandler.this.getContextForEvent(motionEvent);
                return false;
            }
        });
    }

    private static InputDevice.MotionRange getMotionRangeForJoystickAxis(InputDevice inputDevice, int i) {
        InputDevice.MotionRange motionRange = inputDevice.getMotionRange(i, InputDeviceCompat.SOURCE_JOYSTICK);
        return motionRange == null ? inputDevice.getMotionRange(i, 1025) : motionRange;
    }

    private static boolean hasGamepadButtons(InputDevice inputDevice) {
        return (inputDevice.getSources() & 1025) == 1025;
    }

    private static boolean hasJoystickAxes(InputDevice inputDevice) {
        return ((inputDevice.getSources() & InputDeviceCompat.SOURCE_JOYSTICK) != 16777232 || getMotionRangeForJoystickAxis(inputDevice, 0) == null || getMotionRangeForJoystickAxis(inputDevice, 1) == null) ? false : true;
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceAdded(int i) {
        Log.e("ControllerHandler", "Gamepad added");
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceRemoved(int i) {
        Log.e("ControllerHandler", "Gamepad removed");
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceChanged(int i) {
        Log.e("ControllerHandler", "Gamepad changed");
    }

    public boolean handleMotionEvent(MotionEvent motionEvent) {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        float f8;
        InputDeviceContext contextForEvent = getContextForEvent(motionEvent);
        if (contextForEvent == null) {
            return true;
        }
        if ((motionEvent.getSource() & 8194) == 8194) {
            return false;
        }
        if (contextForEvent.leftStickXAxis == -1 || contextForEvent.leftStickYAxis == -1) {
            f = 0.0f;
            f2 = 0.0f;
        } else {
            float axisValue = motionEvent.getAxisValue(contextForEvent.leftStickXAxis);
            f2 = motionEvent.getAxisValue(contextForEvent.leftStickYAxis);
            f = axisValue;
        }
        if (contextForEvent.rightStickXAxis == -1 || contextForEvent.rightStickYAxis == -1) {
            f3 = 0.0f;
            f4 = 0.0f;
        } else {
            float axisValue2 = motionEvent.getAxisValue(contextForEvent.rightStickXAxis);
            f4 = motionEvent.getAxisValue(contextForEvent.rightStickYAxis);
            f3 = axisValue2;
        }
        if (contextForEvent.leftTriggerAxis == -1 || contextForEvent.rightTriggerAxis == -1) {
            f5 = 0.0f;
            f6 = 0.0f;
        } else {
            float axisValue3 = motionEvent.getAxisValue(contextForEvent.leftTriggerAxis);
            f6 = motionEvent.getAxisValue(contextForEvent.rightTriggerAxis);
            f5 = axisValue3;
        }
        if (contextForEvent.hatXAxis == -1 || contextForEvent.hatYAxis == -1) {
            f7 = 0.0f;
            f8 = 0.0f;
        } else {
            float axisValue4 = motionEvent.getAxisValue(15);
            f8 = motionEvent.getAxisValue(16);
            f7 = axisValue4;
        }
        handleAxisSet(contextForEvent, f, f2, f3, f4, f5, f6, f7, f8);
        return true;
    }

    private byte maxByMagnitude(byte b, byte b2) {
        return Math.abs((int) b) > Math.abs((int) b2) ? b : b2;
    }

    private short maxByMagnitude(short s, short s2) {
        return Math.abs((int) s) > Math.abs((int) s2) ? s : s2;
    }

    private void sendControllerInputPacket() {
        ControllerHandlerListener controllerHandlerListener;
        short s = 0;
        byte b = 0;
        byte b2 = 0;
        short s2 = 0;
        short s3 = 0;
        short s4 = 0;
        short s5 = 0;
        for (int i = 0; i < this.inputDeviceContexts.size(); i++) {
            InputDeviceContext valueAt = this.inputDeviceContexts.valueAt(i);
            s = (short) (s | valueAt.inputMap);
            b = (byte) (b | maxByMagnitude(b, valueAt.leftTrigger));
            b2 = (byte) (b2 | maxByMagnitude(b2, valueAt.rightTrigger));
            s2 = (short) (s2 | maxByMagnitude(s2, valueAt.leftStickX));
            s3 = (short) (s3 | maxByMagnitude(s3, valueAt.leftStickY));
            s4 = (short) (s4 | maxByMagnitude(s4, valueAt.rightStickX));
            s5 = (short) (s5 | maxByMagnitude(s5, valueAt.rightStickY));
        }
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("leftTrigger", normalizeByte(b));
            jSONObject.put("rightTrigger", normalizeByte(b2));
            jSONObject.put("leftStickX", normalizeShort(s2));
            jSONObject.put("leftStickY", -normalizeShort(s3));
            jSONObject.put("rightStickX", normalizeShort(s4));
            jSONObject.put("rightStickY", -normalizeShort(s5));
            jSONObject.put("inputMap", (int) s);
            String jSONObject2 = jSONObject.toString();
            if (this.previousPayload.equals(jSONObject2) || (controllerHandlerListener = this.listener) == null) {
                return;
            }
            controllerHandlerListener.controllerData(jSONObject);
            this.previousPayload = jSONObject2;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Vector2d populateCachedVector(float f, float f2) {
        this.inputVector.initialize(f, f2);
        return this.inputVector;
    }

    private void handleDeadZone(Vector2d vector2d, float f) {
        if (vector2d.getMagnitude() <= f) {
            vector2d.initialize(0.0f, 0.0f);
        }
    }

    private void handleAxisSet(InputDeviceContext inputDeviceContext, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        Vector2d populateCachedVector;
        Vector2d populateCachedVector2;
        if (inputDeviceContext.leftStickXAxis != -1 && inputDeviceContext.leftStickYAxis != -1) {
            handleDeadZone(populateCachedVector(f, f2), 0.1f);
            inputDeviceContext.leftStickX = (short) (populateCachedVector2.getX() * 32766.0f);
            inputDeviceContext.leftStickY = (short) ((-populateCachedVector2.getY()) * 32766.0f);
        }
        if (inputDeviceContext.rightStickXAxis != -1 && inputDeviceContext.rightStickYAxis != -1) {
            handleDeadZone(populateCachedVector(f3, f4), 0.1f);
            inputDeviceContext.rightStickX = (short) (populateCachedVector.getX() * 32766.0f);
            inputDeviceContext.rightStickY = (short) ((-populateCachedVector.getY()) * 32766.0f);
        }
        if (inputDeviceContext.leftTriggerAxis != -1 && inputDeviceContext.rightTriggerAxis != -1) {
            if (f5 != 0.0f) {
                inputDeviceContext.leftTriggerAxisUsed = true;
            }
            if (f6 != 0.0f) {
                inputDeviceContext.rightTriggerAxisUsed = true;
            }
            if (inputDeviceContext.triggersIdleNegative) {
                if (inputDeviceContext.leftTriggerAxisUsed) {
                    f5 = (f5 + 1.0f) / 2.0f;
                }
                if (inputDeviceContext.rightTriggerAxisUsed) {
                    f6 = (f6 + 1.0f) / 2.0f;
                }
            }
            inputDeviceContext.leftTrigger = (byte) (f5 * 127.0f);
            inputDeviceContext.rightTrigger = (byte) (f6 * 127.0f);
        }
        if (inputDeviceContext.hatXAxis != -1 && inputDeviceContext.hatYAxis != -1) {
            inputDeviceContext.inputMap = (short) (inputDeviceContext.inputMap & (-13));
            double d = f7;
            if (d < -0.5d) {
                inputDeviceContext.inputMap = (short) (inputDeviceContext.inputMap | 4);
                inputDeviceContext.hatXAxisUsed = true;
            } else if (d > 0.5d) {
                inputDeviceContext.inputMap = (short) (inputDeviceContext.inputMap | 8);
                inputDeviceContext.hatXAxisUsed = true;
            }
            inputDeviceContext.inputMap = (short) (inputDeviceContext.inputMap & (-4));
            double d2 = f8;
            if (d2 < -0.5d) {
                inputDeviceContext.inputMap = (short) (inputDeviceContext.inputMap | 1);
                inputDeviceContext.hatYAxisUsed = true;
            } else if (d2 > 0.5d) {
                inputDeviceContext.inputMap = (short) (inputDeviceContext.inputMap | 2);
                inputDeviceContext.hatYAxisUsed = true;
            }
        }
        sendControllerInputPacket();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public InputDeviceContext getContextForEvent(InputEvent inputEvent) {
        if (inputEvent.getDeviceId() == 0 || inputEvent.getDevice() == null) {
            return null;
        }
        InputDeviceContext inputDeviceContext = this.inputDeviceContexts.get(inputEvent.getDeviceId());
        if (inputDeviceContext != null) {
            return inputDeviceContext;
        }
        InputDeviceContext createInputDeviceContextForDevice = createInputDeviceContextForDevice(inputEvent.getDevice());
        this.inputDeviceContexts.put(inputEvent.getDeviceId(), createInputDeviceContextForDevice);
        return createInputDeviceContextForDevice;
    }

    public boolean handleButtonDown(KeyEvent keyEvent) {
        Log.e("HERE", "HANDLE KEY DOWN");
        if ((keyEvent.getSource() & 1025) != 1025) {
            Log.e("HERE", "Not gamepad");
            return false;
        }
        InputDeviceContext contextForEvent = getContextForEvent(keyEvent);
        if (contextForEvent == null) {
            Log.e("HERE", "null context");
            return true;
        }
        int handleRemapping = handleRemapping(contextForEvent, keyEvent);
        if (handleRemapping == 0) {
            Log.e("HERE", "0 keycode");
            return true;
        }
        if (handleRemapping != 4) {
            if (handleRemapping != 82) {
                if (handleRemapping != 96) {
                    if (handleRemapping == 97) {
                        contextForEvent.inputMap = (short) (contextForEvent.inputMap | ControllerPacket.B_FLAG);
                    } else if (handleRemapping == 99) {
                        contextForEvent.inputMap = (short) (contextForEvent.inputMap | 16384);
                    } else if (handleRemapping != 100) {
                        switch (handleRemapping) {
                            case 19:
                                if (!contextForEvent.hatYAxisUsed) {
                                    contextForEvent.inputMap = (short) (contextForEvent.inputMap | 1);
                                    break;
                                } else {
                                    return true;
                                }
                            case 20:
                                if (!contextForEvent.hatYAxisUsed) {
                                    contextForEvent.inputMap = (short) (contextForEvent.inputMap | 2);
                                    break;
                                } else {
                                    return true;
                                }
                            case 21:
                                if (!contextForEvent.hatXAxisUsed) {
                                    contextForEvent.inputMap = (short) (contextForEvent.inputMap | 4);
                                    break;
                                } else {
                                    return true;
                                }
                            case 22:
                                if (!contextForEvent.hatXAxisUsed) {
                                    contextForEvent.inputMap = (short) (contextForEvent.inputMap | 8);
                                    break;
                                } else {
                                    return true;
                                }
                            case 23:
                                break;
                            default:
                                switch (handleRemapping) {
                                    case 102:
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap | ControllerPacket.LB_FLAG);
                                        break;
                                    case 103:
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap | ControllerPacket.RB_FLAG);
                                        break;
                                    case 104:
                                        if (!contextForEvent.leftTriggerAxisUsed) {
                                            contextForEvent.leftTrigger = (byte) -1;
                                            break;
                                        } else {
                                            return true;
                                        }
                                    case 105:
                                        if (!contextForEvent.rightTriggerAxisUsed) {
                                            contextForEvent.rightTrigger = (byte) -1;
                                            break;
                                        } else {
                                            return true;
                                        }
                                    case CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256 /* 106 */:
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap | 64);
                                        break;
                                    case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256 /* 107 */:
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap | ControllerPacket.RS_CLK_FLAG);
                                        break;
                                    case 108:
                                        break;
                                    case 109:
                                        break;
                                    case MediaError.DetailedErrorCode.SOURCE_BUFFER_FAILURE /* 110 */:
                                        contextForEvent.hasMode = true;
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap | 1024);
                                        break;
                                    default:
                                        return false;
                                }
                        }
                    } else {
                        contextForEvent.inputMap = (short) (contextForEvent.inputMap | Short.MIN_VALUE);
                    }
                    sendControllerInputPacket();
                    return true;
                }
                contextForEvent.inputMap = (short) (contextForEvent.inputMap | ControllerPacket.A_FLAG);
                sendControllerInputPacket();
                return true;
            }
            if (keyEvent.getRepeatCount() == 0) {
                contextForEvent.startDownTime = keyEvent.getEventTime();
            }
            contextForEvent.inputMap = (short) (contextForEvent.inputMap | 16);
            sendControllerInputPacket();
            return true;
        }
        contextForEvent.hasSelect = true;
        contextForEvent.inputMap = (short) (contextForEvent.inputMap | 32);
        sendControllerInputPacket();
        return true;
    }

    public boolean handleButtonUp(KeyEvent keyEvent) {
        if ((keyEvent.getSource() & 1025) != 1025) {
            Log.e("HEREUP", "Not gamepad");
            return false;
        }
        InputDeviceContext contextForEvent = getContextForEvent(keyEvent);
        if (contextForEvent == null) {
            Log.e("HEREUP", "null context");
            return true;
        }
        int handleRemapping = handleRemapping(contextForEvent, keyEvent);
        if (handleRemapping == 0) {
            Log.e("HEREUP", "0 keycode");
            return true;
        }
        if (handleRemapping != 4) {
            if (handleRemapping != 82) {
                if (handleRemapping != 96) {
                    if (handleRemapping == 97) {
                        contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-8193));
                    } else if (handleRemapping == 99) {
                        contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-16385));
                    } else if (handleRemapping != 100) {
                        switch (handleRemapping) {
                            case 19:
                                if (!contextForEvent.hatYAxisUsed) {
                                    contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-2));
                                    break;
                                } else {
                                    return true;
                                }
                            case 20:
                                if (!contextForEvent.hatYAxisUsed) {
                                    contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-3));
                                    break;
                                } else {
                                    return true;
                                }
                            case 21:
                                if (!contextForEvent.hatXAxisUsed) {
                                    contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-5));
                                    break;
                                } else {
                                    return true;
                                }
                            case 22:
                                if (!contextForEvent.hatXAxisUsed) {
                                    contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-9));
                                    break;
                                } else {
                                    return true;
                                }
                            case 23:
                                break;
                            default:
                                switch (handleRemapping) {
                                    case 102:
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-257));
                                        break;
                                    case 103:
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-513));
                                        break;
                                    case 104:
                                        if (!contextForEvent.leftTriggerAxisUsed) {
                                            contextForEvent.leftTrigger = (byte) 0;
                                            break;
                                        } else {
                                            return true;
                                        }
                                    case 105:
                                        if (!contextForEvent.rightTriggerAxisUsed) {
                                            contextForEvent.rightTrigger = (byte) 0;
                                            break;
                                        } else {
                                            return true;
                                        }
                                    case CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256 /* 106 */:
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-65));
                                        break;
                                    case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256 /* 107 */:
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-129));
                                        break;
                                    case 108:
                                        break;
                                    case 109:
                                        break;
                                    case MediaError.DetailedErrorCode.SOURCE_BUFFER_FAILURE /* 110 */:
                                        contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-1025));
                                        break;
                                    default:
                                        return false;
                                }
                        }
                    } else {
                        contextForEvent.inputMap = (short) (contextForEvent.inputMap & ShortCompanionObject.MAX_VALUE);
                    }
                    sendControllerInputPacket();
                    return true;
                }
                contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-4097));
                sendControllerInputPacket();
                return true;
            }
            contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-17));
            sendControllerInputPacket();
            return true;
        }
        contextForEvent.inputMap = (short) (contextForEvent.inputMap & (-33));
        sendControllerInputPacket();
        return true;
    }

    private int handleRemapping(InputDeviceContext inputDeviceContext, KeyEvent keyEvent) {
        if (inputDeviceContext.ignoreBack && keyEvent.getKeyCode() == 4) {
            return -1;
        }
        if (inputDeviceContext.vendorId == 11720 && keyEvent.getScanCode() == 306) {
            return MediaError.DetailedErrorCode.SOURCE_BUFFER_FAILURE;
        }
        if ((inputDeviceContext.vendorId == 1406 && inputDeviceContext.productId == 8201 && Build.VERSION.SDK_INT < 29) || (inputDeviceContext.vendorId == 3853 && inputDeviceContext.productId == 193)) {
            switch (keyEvent.getScanCode()) {
                case HttpStatus.SC_NOT_MODIFIED /* 304 */:
                    return 96;
                case 305:
                    return 97;
                case 306:
                    return 99;
                case 307:
                    return 100;
                case 308:
                    return 102;
                case 309:
                    return 103;
                case 310:
                    return 104;
                case MediaError.DetailedErrorCode.HLS_NETWORK_MASTER_PLAYLIST /* 311 */:
                    return 105;
                case MediaError.DetailedErrorCode.HLS_NETWORK_PLAYLIST /* 312 */:
                    return 109;
                case MediaError.DetailedErrorCode.HLS_NETWORK_NO_KEY_RESPONSE /* 313 */:
                    return 108;
                case MediaError.DetailedErrorCode.HLS_NETWORK_KEY_LOAD /* 314 */:
                    return CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256;
                case MediaError.DetailedErrorCode.HLS_NETWORK_INVALID_SEGMENT /* 315 */:
                    return CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256;
                case 317:
                    return MediaError.DetailedErrorCode.SOURCE_BUFFER_FAILURE;
            }
        }
        if (inputDeviceContext.usesLinuxGamepadStandardFaceButtons) {
            switch (keyEvent.getScanCode()) {
                case HttpStatus.SC_NOT_MODIFIED /* 304 */:
                    return 96;
                case 305:
                    return 97;
                case 307:
                    return 100;
                case 308:
                    return 99;
            }
        }
        if (inputDeviceContext.isNonStandardDualShock4) {
            switch (keyEvent.getScanCode()) {
                case HttpStatus.SC_NOT_MODIFIED /* 304 */:
                    return 99;
                case 305:
                    return 96;
                case 306:
                    return 97;
                case 307:
                    return 100;
                case 308:
                    return 102;
                case 309:
                    return 103;
                case 310:
                case MediaError.DetailedErrorCode.HLS_NETWORK_MASTER_PLAYLIST /* 311 */:
                default:
                    return 0;
                case MediaError.DetailedErrorCode.HLS_NETWORK_PLAYLIST /* 312 */:
                    return 109;
                case MediaError.DetailedErrorCode.HLS_NETWORK_NO_KEY_RESPONSE /* 313 */:
                    return 108;
                case MediaError.DetailedErrorCode.HLS_NETWORK_KEY_LOAD /* 314 */:
                    return CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256;
                case MediaError.DetailedErrorCode.HLS_NETWORK_INVALID_SEGMENT /* 315 */:
                    return CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256;
                case MediaError.DetailedErrorCode.HLS_SEGMENT_PARSING /* 316 */:
                    return MediaError.DetailedErrorCode.SOURCE_BUFFER_FAILURE;
            }
        }
        if (inputDeviceContext.isServal && keyEvent.getKeyCode() == 0) {
            int scanCode = keyEvent.getScanCode();
            if (scanCode == 314) {
                return 109;
            }
            if (scanCode == 315) {
                return 108;
            }
        } else if (inputDeviceContext.isNonStandardXboxBtController) {
            int scanCode2 = keyEvent.getScanCode();
            if (scanCode2 != 139) {
                switch (scanCode2) {
                    case 306:
                        return 99;
                    case 307:
                        return 100;
                    case 308:
                        return 102;
                    case 309:
                        return 103;
                    case 310:
                        return 109;
                    case MediaError.DetailedErrorCode.HLS_NETWORK_MASTER_PLAYLIST /* 311 */:
                        return 108;
                    case MediaError.DetailedErrorCode.HLS_NETWORK_PLAYLIST /* 312 */:
                        return CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256;
                    case MediaError.DetailedErrorCode.HLS_NETWORK_NO_KEY_RESPONSE /* 313 */:
                        return CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256;
                    default:
                        if (keyEvent.getKeyCode() == 82) {
                            return MediaError.DetailedErrorCode.SOURCE_BUFFER_FAILURE;
                        }
                        break;
                }
            } else {
                return MediaError.DetailedErrorCode.SOURCE_BUFFER_FAILURE;
            }
        } else if (inputDeviceContext.vendorId == 2821 && (inputDeviceContext.productId == 30976 || inputDeviceContext.productId == 30978)) {
            switch (keyEvent.getScanCode()) {
                case MediaRouter.GlobalMediaRouter.CallbackHandler.MSG_ROUTE_ANOTHER_SELECTED /* 264 */:
                case 266:
                    return 108;
                case 265:
                case 267:
                    return 109;
            }
        }
        if (inputDeviceContext.hatXAxis == -1 && inputDeviceContext.hatYAxis == -1 && keyEvent.getKeyCode() == 0) {
            switch (keyEvent.getScanCode()) {
                case 704:
                    return 21;
                case 705:
                    return 22;
                case 706:
                    return 19;
                case 707:
                    return 20;
            }
        }
        int keyCode = keyEvent.getKeyCode();
        int i = (keyCode != 4 || keyEvent.hasNoModifiers() || (keyEvent.getFlags() & 2) == 0) ? keyCode : 97;
        if (i == 108 || i == 82) {
            inputDeviceContext.backIsStart = false;
        } else if (i == 109) {
            inputDeviceContext.modeIsSelect = false;
        } else if (inputDeviceContext.backIsStart && i == 4) {
            return 108;
        } else {
            if (inputDeviceContext.modeIsSelect && i == 110) {
                return 109;
            }
            if (inputDeviceContext.searchIsMode && i == 84) {
                return MediaError.DetailedErrorCode.SOURCE_BUFFER_FAILURE;
            }
        }
        return i;
    }

    private static boolean isExternal(InputDevice inputDevice) {
        if (Build.MODEL.equals("Tinker Board")) {
            return true;
        }
        String name = inputDevice.getName();
        if (name.contains("gpio") || name.contains("joy_key") || name.contains("keypad") || name.equalsIgnoreCase("NVIDIA Corporation NVIDIA Controller v01.01") || name.equalsIgnoreCase("NVIDIA Corporation NVIDIA Controller v01.02")) {
            Log.e("Controllerhelper", inputDevice.getName() + " is internal by hardcoded mapping");
            return false;
        } else if (Build.VERSION.SDK_INT >= 29) {
            return inputDevice.isExternal();
        } else {
            try {
                return ((Boolean) inputDevice.getClass().getMethod("isExternal", new Class[0]).invoke(inputDevice, new Object[0])).booleanValue();
            } catch (ClassCastException e) {
                e.printStackTrace();
                return true;
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                return true;
            } catch (NoSuchMethodException e3) {
                e3.printStackTrace();
                return true;
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
                return true;
            }
        }
    }

    private InputDeviceContext createInputDeviceContextForDevice(InputDevice inputDevice) {
        boolean z;
        InputDeviceContext inputDeviceContext = new InputDeviceContext(inputDevice);
        String name = inputDevice.getName();
        inputDeviceContext.inputDevice = inputDevice;
        inputDeviceContext.name = name;
        inputDeviceContext.id = inputDevice.getId();
        inputDeviceContext.external = isExternal(inputDevice);
        inputDeviceContext.vendorId = inputDevice.getVendorId();
        inputDeviceContext.productId = inputDevice.getProductId();
        boolean[] hasKeys = inputDevice.hasKeys(MediaError.DetailedErrorCode.SOURCE_BUFFER_FAILURE, 109, 4, 0);
        inputDeviceContext.hasMode = hasKeys[0];
        inputDeviceContext.hasSelect = hasKeys[1] || hasKeys[2];
        inputDeviceContext.leftStickXAxis = 0;
        inputDeviceContext.leftStickYAxis = 1;
        if (getMotionRangeForJoystickAxis(inputDevice, inputDeviceContext.leftStickXAxis) != null && getMotionRangeForJoystickAxis(inputDevice, inputDeviceContext.leftStickYAxis) != null) {
            this.hasGameController = true;
            inputDeviceContext.hasJoystickAxes = true;
        }
        InputDevice.MotionRange motionRangeForJoystickAxis = getMotionRangeForJoystickAxis(inputDevice, 17);
        InputDevice.MotionRange motionRangeForJoystickAxis2 = getMotionRangeForJoystickAxis(inputDevice, 18);
        InputDevice.MotionRange motionRangeForJoystickAxis3 = getMotionRangeForJoystickAxis(inputDevice, 23);
        InputDevice.MotionRange motionRangeForJoystickAxis4 = getMotionRangeForJoystickAxis(inputDevice, 22);
        InputDevice.MotionRange motionRangeForJoystickAxis5 = getMotionRangeForJoystickAxis(inputDevice, 19);
        if (motionRangeForJoystickAxis != null && motionRangeForJoystickAxis2 != null) {
            inputDeviceContext.leftTriggerAxis = 17;
            inputDeviceContext.rightTriggerAxis = 18;
        } else if (motionRangeForJoystickAxis3 != null && motionRangeForJoystickAxis4 != null) {
            inputDeviceContext.leftTriggerAxis = 23;
            inputDeviceContext.rightTriggerAxis = 22;
        } else if (motionRangeForJoystickAxis3 != null && motionRangeForJoystickAxis5 != null) {
            inputDeviceContext.leftTriggerAxis = 23;
            inputDeviceContext.rightTriggerAxis = 19;
        } else {
            InputDevice.MotionRange motionRangeForJoystickAxis6 = getMotionRangeForJoystickAxis(inputDevice, 12);
            InputDevice.MotionRange motionRangeForJoystickAxis7 = getMotionRangeForJoystickAxis(inputDevice, 13);
            if (motionRangeForJoystickAxis6 != null && motionRangeForJoystickAxis7 != null && name != null) {
                if (inputDevice.getVendorId() != 1356) {
                    z = true;
                } else if (inputDevice.hasKeys(98)[0]) {
                    Log.e("ControllerHelper", "Detected non-standard DualShock 4 mapping");
                    z = true;
                    inputDeviceContext.isNonStandardDualShock4 = true;
                } else {
                    z = true;
                    Log.e("ControllerHelper", "Detected DualShock 4 (Linux standard mapping)");
                    inputDeviceContext.usesLinuxGamepadStandardFaceButtons = true;
                }
                if (inputDeviceContext.isNonStandardDualShock4) {
                    inputDeviceContext.leftTriggerAxis = 12;
                    inputDeviceContext.rightTriggerAxis = 13;
                    inputDeviceContext.hasSelect = z;
                    inputDeviceContext.hasMode = z;
                } else {
                    inputDeviceContext.rightStickXAxis = 12;
                    inputDeviceContext.rightStickYAxis = 13;
                    if (getMotionRangeForJoystickAxis(inputDevice, 11) != null && getMotionRangeForJoystickAxis(inputDevice, 14) != null) {
                        inputDeviceContext.leftTriggerAxis = 11;
                        inputDeviceContext.rightTriggerAxis = 14;
                    }
                    z = true;
                }
                inputDeviceContext.triggersIdleNegative = z;
            }
        }
        if (inputDeviceContext.rightStickXAxis == -1 && inputDeviceContext.rightStickYAxis == -1) {
            InputDevice.MotionRange motionRangeForJoystickAxis8 = getMotionRangeForJoystickAxis(inputDevice, 11);
            InputDevice.MotionRange motionRangeForJoystickAxis9 = getMotionRangeForJoystickAxis(inputDevice, 14);
            if (motionRangeForJoystickAxis8 != null && motionRangeForJoystickAxis9 != null) {
                inputDeviceContext.rightStickXAxis = 11;
                inputDeviceContext.rightStickYAxis = 14;
            } else {
                InputDevice.MotionRange motionRangeForJoystickAxis10 = getMotionRangeForJoystickAxis(inputDevice, 12);
                InputDevice.MotionRange motionRangeForJoystickAxis11 = getMotionRangeForJoystickAxis(inputDevice, 13);
                if (motionRangeForJoystickAxis10 != null && motionRangeForJoystickAxis11 != null) {
                    inputDeviceContext.rightStickXAxis = 12;
                    inputDeviceContext.rightStickYAxis = 13;
                }
            }
        }
        InputDevice.MotionRange motionRangeForJoystickAxis12 = getMotionRangeForJoystickAxis(inputDevice, 15);
        InputDevice.MotionRange motionRangeForJoystickAxis13 = getMotionRangeForJoystickAxis(inputDevice, 16);
        if (motionRangeForJoystickAxis12 != null && motionRangeForJoystickAxis13 != null) {
            inputDeviceContext.hatXAxis = 15;
            inputDeviceContext.hatYAxis = 16;
        }
        if (inputDevice.getVendorId() == 6353 && inputDevice.getProductId() == 11328) {
            inputDeviceContext.backIsStart = true;
            inputDeviceContext.modeIsSelect = true;
            inputDeviceContext.hasSelect = true;
            inputDeviceContext.hasMode = false;
        }
        inputDeviceContext.ignoreBack = shouldIgnoreBack(inputDevice);
        if (name != null) {
            if (name.contains("ASUS Gamepad")) {
                boolean[] hasKeys2 = inputDevice.hasKeys(108, 82, 0);
                if (!hasKeys2[0] && !hasKeys2[1]) {
                    inputDeviceContext.backIsStart = true;
                    inputDeviceContext.modeIsSelect = true;
                    inputDeviceContext.hasSelect = true;
                    inputDeviceContext.hasMode = false;
                }
            } else if (name.contains("SHIELD") || name.contains("NVIDIA Controller")) {
                if (name.contains("NVIDIA Controller v01.03") || name.contains("NVIDIA Controller v01.04")) {
                    inputDeviceContext.searchIsMode = true;
                    inputDeviceContext.hasMode = true;
                }
            } else if (name.contains("Razer Serval")) {
                inputDeviceContext.isServal = true;
                inputDeviceContext.hasMode = true;
                inputDeviceContext.hasSelect = true;
            } else if (name.equals("Xbox Wireless Controller") && motionRangeForJoystickAxis4 == null) {
                inputDeviceContext.isNonStandardXboxBtController = true;
                inputDeviceContext.hasMode = true;
                inputDeviceContext.hasSelect = true;
            }
        }
        return this.rumbleHelper.addDevice(inputDeviceContext, inputDevice);
    }

    private boolean shouldIgnoreBack(InputDevice inputDevice) {
        String name = inputDevice.getName();
        if (name.contains("Razer Serval")) {
            return true;
        }
        if (hasJoystickAxes(inputDevice) || !name.toLowerCase().contains("remote")) {
            if (isExternal(inputDevice)) {
                return (hasJoystickAxes(inputDevice) || hasGamepadButtons(inputDevice)) ? false : true;
            }
            InputManager inputManager = (InputManager) this.context.getSystemService("input");
            boolean z = false;
            boolean z2 = false;
            for (int i : inputManager.getInputDeviceIds()) {
                InputDevice inputDevice2 = inputManager.getInputDevice(i);
                if (inputDevice2 != null && !isExternal(inputDevice2)) {
                    if (inputDevice2.hasKeys(109)[0]) {
                        z2 = true;
                    }
                    if (hasGamepadButtons(inputDevice2)) {
                        z = true;
                    }
                }
            }
            return !z || z2;
        }
        return true;
    }
}
