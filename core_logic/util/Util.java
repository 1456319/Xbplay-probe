package util;

import android.util.Log;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;
import com.google.android.gms.cast.MediaError;
import com.google.common.base.Ascii;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;
/* loaded from: /app/base.apk/classes5.dex */
public final class Util {
    public static byte[] PRE_SALT = {-42, 55, -15, -86, -30, -16, 65, -116};
    public static byte[] POST_SALT = {-88, -8, Ascii.SUB, 87, 78, 34, -118, -73};
    public static int XBOX_PORT = 5050;
    public static String XBOX_DISCOVERY_IP = "255.255.255.255";

    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer wrap = ByteBuffer.wrap(new byte[16]);
        wrap.putLong(uuid.getMostSignificantBits());
        wrap.putLong(uuid.getLeastSignificantBits());
        return wrap.array();
    }

    public static byte[] removeTrailingBytes(byte[] bArr, int i) {
        try {
            return Arrays.copyOfRange(bArr, 0, bArr.length - i);
        } catch (Exception e) {
            Log.e("ERR", e.getMessage());
            return bArr;
        }
    }

    public static byte[] copyHeadBytes(byte[] bArr, int i) {
        try {
            return Arrays.copyOfRange(bArr, 0, i);
        } catch (Exception e) {
            Log.e("ERR", e.getMessage());
            return bArr;
        }
    }

    public static byte[] readInputArray(byte[] bArr, int i, int i2) {
        return Arrays.copyOfRange(bArr, i, i2);
    }

    public static String hexByteArrayToASCIIString(byte[] bArr) {
        if (bArr == null) {
            return "NULL byte array sent to print ASC method";
        }
        String str = "";
        for (int i = 0; i < bArr.length; i++) {
            str = str + ((char) bArr[i]);
        }
        return str;
    }

    public static String byteArrayToHexString(byte[] bArr, boolean z) {
        if (bArr == null) {
            return "NULL byte array sent to print method";
        }
        String str = "";
        for (byte b : bArr) {
            String format = String.format("%02x", Byte.valueOf(b));
            if (z && format.length() % 2 != 0) {
                format = SessionDescription.SUPPORTED_SDP_VERSION + format;
            }
            str = (str + format) + ", ";
        }
        return str;
    }

    public static byte[] calcPayloadLength(byte[] bArr) {
        return intLengthTo2Bytes(bArr.length);
    }

    public static int unsignedIntToLong(byte[] bArr) {
        return (bArr[3] & 255) | ((((((bArr[0] & 255) << 8) | (bArr[1] & 255)) << 8) | (bArr[2] & 255)) << 8);
    }

    public static int unsignedShortToInt(byte[] bArr) {
        return (bArr[1] & 255) | ((bArr[0] & 255) << 8);
    }

    public static byte[] intLengthTo2Bytes(int i) {
        return new byte[]{(byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    public static byte[] intLengthTo2BytesLE(int i) {
        return new byte[]{(byte) (i & 255), (byte) ((i >> 8) & 255)};
    }

    public static byte[] intLengthTo4Bytes(int i) {
        return new byte[]{(byte) ((i >> 24) & 65535), (byte) ((i >> 16) & 65535), (byte) ((i >> 8) & 65535), (byte) (i & 65535)};
    }

    public static byte[] intLengthTo4BytesLE(int i) {
        return new byte[]{(byte) (i & 65535), (byte) ((i >> 8) & 65535), (byte) ((i >> 16) & 65535), (byte) ((i >> 24) & 65535)};
    }

    public static byte[] longLengthTo8BytesLE(long j) {
        return new byte[]{(byte) (j & 65535), (byte) ((j >> 8) & 65535), (byte) ((j >> 16) & 65535), (byte) ((j >> 24) & 65535), (byte) ((j >> 32) & 65535), (byte) ((j >> 40) & 65535), (byte) ((j >> 48) & 65535), (byte) ((j >> 56) & 65535)};
    }

    public static int byteArrayToIntLE(byte[] bArr) {
        if (bArr.length < 4) {
            int length = 4 - bArr.length;
            for (int i = 0; i < length; i++) {
                bArr = concat(bArr, new byte[]{0});
            }
        }
        try {
            return ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } catch (Exception e) {
            Log.e(MediaError.ERROR_TYPE_ERROR, "Error getting LE length of byte array: " + e.getMessage());
            return 0;
        }
    }

    public static int byteArrayToIntBE(byte[] bArr) {
        try {
            return ByteBuffer.wrap(bArr).getInt();
        } catch (Exception e) {
            Log.e(MediaError.ERROR_TYPE_ERROR, "Error getting BE length of byte array: " + e.getMessage());
            return 0;
        }
    }

    public static String byteArrayToBinString(byte[] bArr) {
        char[] cArr = new char[bArr.length * 8];
        for (int i = 0; i < bArr.length; i++) {
            byte b = bArr[i];
            int i2 = i << 3;
            int i3 = 1;
            for (int i4 = 7; i4 >= 0; i4--) {
                if ((b & i3) == 0) {
                    cArr[i2 + i4] = '0';
                } else {
                    cArr[i2 + i4] = '1';
                }
                i3 <<= 1;
            }
        }
        return String.valueOf(cArr);
    }

    public static String[] splitStringEvery(String str, int i) {
        int ceil = (int) Math.ceil(str.length() / i);
        String[] strArr = new String[ceil];
        int i2 = ceil - 1;
        int i3 = 0;
        int i4 = 0;
        while (i3 < i2) {
            int i5 = i4 + i;
            strArr[i3] = str.substring(i4, i5);
            i3++;
            i4 = i5;
        }
        strArr[i2] = str.substring(i4);
        return strArr;
    }

    public static byte[] hexStringToByteArray(String str) {
        int length = str.length() / 2;
        byte[] bArr = new byte[length];
        for (int i = 0; i < length; i++) {
            int i2 = i * 2;
            bArr[i] = (byte) Integer.parseInt(str.substring(i2, i2 + 2), 16);
        }
        return bArr;
    }

    public static byte[] concat(byte[] bArr, byte[] bArr2) {
        int length = bArr.length;
        int length2 = bArr2.length;
        byte[] copyOf = Arrays.copyOf(bArr, length + length2);
        System.arraycopy(bArr2, 0, copyOf, length, length2);
        return copyOf;
    }

    public static byte[] stringToByteArray(String str) {
        String[] splitStringEvery = splitStringEvery(str, 1);
        byte[] bArr = new byte[splitStringEvery.length];
        for (int i = 0; i < splitStringEvery.length; i++) {
            bArr[i] = splitStringEvery[i].getBytes()[0];
        }
        return bArr;
    }

    public static ByteArrayOutputStream byteArrayToOutputStream(byte[] bArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(bArr);
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream;
    }

    public static byte[] createNullByteArray(int i) {
        byte[] bArr = new byte[i];
        Arrays.fill(bArr, (byte) 0);
        return bArr;
    }
}
