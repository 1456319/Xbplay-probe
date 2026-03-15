package constants;

import com.google.common.base.Ascii;
import com.google.common.primitives.SignedBytes;
/* loaded from: /app/base.apk/classes4.dex */
public class ChannelTypes {
    public static final byte[] SYSTEM_INPUT = {-6, 32, -72, -54, 102, -5, 70, -32, -83, -74, Ascii.VT, -105, -118, 89, -45, 95};
    public static final byte[] SYSTEM_MEDIA = {72, -87, -54, 36, -21, 109, 78, Ascii.DC2, -116, 67, -43, 116, 105, -19, -45, -51};
    public static final byte[] SYSTEM_BROADCAST = {-74, -95, Ascii.ETB, -40, -11, -30, 69, -41, -122, 46, -113, -40, -29, Ascii.NAK, NanoPayloadTypes.UDP_HANDSHAKE, 118};
    public static final byte[] TEXT_INPUT = {122, -13, -26, -94, 72, -117, SignedBytes.MAX_POWER_OF_TWO, -53, -87, 49, 121, -64, 75, 125, -93, -96};
    public static final byte[] ACK_CHANNEL = {16, 0, 0, 0, 0, 0, 0, 0};
}
