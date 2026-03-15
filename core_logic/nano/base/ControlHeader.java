package nano.base;

import android.util.Log;
import com.google.android.gms.cast.MediaError;
import java.io.ByteArrayOutputStream;
import org.spongycastle.util.Arrays;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class ControlHeader {
    private byte[] fullData;
    private int offset;
    private byte[] payload;
    public byte[] prevSequenceNumber;
    public byte[] type;
    public byte[] unknown1;
    public byte[] unknown2;

    public ControlHeader(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        this.unknown1 = Util.intLengthTo2BytesLE(1);
        this.unknown2 = Util.intLengthTo2BytesLE(1406);
        this.offset = 0;
        this.type = bArr;
        this.payload = bArr2;
        this.prevSequenceNumber = bArr3;
    }

    public byte[] serialize() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        safeWrite(byteArrayOutputStream, this.prevSequenceNumber);
        safeWrite(byteArrayOutputStream, this.unknown1);
        safeWrite(byteArrayOutputStream, this.unknown2);
        safeWrite(byteArrayOutputStream, this.type);
        safeWrite(byteArrayOutputStream, this.payload);
        return byteArrayOutputStream.toByteArray();
    }

    private void safeWrite(ByteArrayOutputStream byteArrayOutputStream, byte[] bArr) {
        if (bArr != null) {
            try {
                byteArrayOutputStream.write(bArr);
            } catch (Exception e) {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Error serializing VideoFormat data: " + e.getMessage());
            }
        }
    }

    public ControlHeader(byte[] bArr) {
        this.unknown1 = Util.intLengthTo2BytesLE(1);
        this.unknown2 = Util.intLengthTo2BytesLE(1406);
        this.offset = 0;
        this.fullData = bArr;
        if (bArr != null) {
            this.prevSequenceNumber = readByteArray(4);
            this.unknown1 = readByteArray(2);
            this.unknown2 = readByteArray(2);
            this.type = readByteArray(2);
            return;
        }
        Log.e(MediaError.ERROR_TYPE_ERROR, "Invalid Control header detected!!!");
    }

    private byte[] readByteArray(int i) {
        byte[] bArr = this.fullData;
        int i2 = this.offset;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i2, i2 + i);
        this.offset += i;
        return copyOfRange;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void printData(String str) {
        Log.i("ControlHeader", str);
        Log.i("ControlHeader", "prevSequenceNumber: " + Util.byteArrayToHexString(this.prevSequenceNumber, true));
        Log.i("ControlHeader", "unknown1: " + Util.byteArrayToHexString(this.unknown1, true));
        Log.i("ControlHeader", "unknown2: " + Util.byteArrayToHexString(this.unknown2, true));
        Log.i("ControlHeader", "type: " + Util.byteArrayToHexString(this.type, true));
        Log.i("ControlHeader", "payload: " + Util.byteArrayToHexString(this.payload, true));
    }
}
