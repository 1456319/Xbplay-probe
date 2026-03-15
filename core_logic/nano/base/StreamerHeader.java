package nano.base;

import android.util.Log;
import com.google.android.gms.cast.MediaError;
import java.io.ByteArrayOutputStream;
import org.spongycastle.util.Arrays;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class StreamerHeader {
    public byte[] flags;
    private byte[] fullData;
    private int offset = 0;
    public byte[] payloadLength;
    public byte[] prevSequenceNumber;
    public byte[] sequenceNumber;
    private byte[] streamerPayload;
    public byte[] type;

    public StreamerHeader() {
    }

    public StreamerHeader(StreamerHeader streamerHeader, byte[] bArr, int i) {
        this.flags = streamerHeader.flags;
        this.sequenceNumber = Util.intLengthTo4BytesLE(i);
        this.prevSequenceNumber = streamerHeader.sequenceNumber;
        this.type = bArr;
    }

    public byte[] serialize() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        safeWrite(byteArrayOutputStream, this.flags);
        safeWrite(byteArrayOutputStream, this.sequenceNumber);
        safeWrite(byteArrayOutputStream, this.prevSequenceNumber);
        safeWrite(byteArrayOutputStream, this.type);
        safeWrite(byteArrayOutputStream, this.payloadLength);
        safeWrite(byteArrayOutputStream, this.streamerPayload);
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

    public StreamerHeader(byte[] bArr) {
        this.fullData = bArr;
        if (bArr != null && bArr.length >= 20) {
            byte[] readByteArray = readByteArray(4);
            this.flags = readByteArray;
            if (Arrays.areEqual(readByteArray, new byte[]{3, 0, 0, 0})) {
                this.sequenceNumber = readByteArray(4);
                this.prevSequenceNumber = readByteArray(4);
            }
            byte[] readByteArray2 = readByteArray(4);
            this.type = readByteArray2;
            if (Arrays.areEqual(readByteArray2, new byte[]{0, 0, 0, 0})) {
                return;
            }
            byte[] readByteArray3 = readByteArray(4);
            this.payloadLength = readByteArray3;
            this.streamerPayload = readByteArray(Util.byteArrayToIntLE(readByteArray3));
            return;
        }
        Log.e(MediaError.ERROR_TYPE_ERROR, "Invalid Streamer header detected!!!");
    }

    private byte[] readByteArray(int i) {
        byte[] bArr = this.fullData;
        int i2 = this.offset;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i2, i2 + i);
        this.offset += i;
        return copyOfRange;
    }

    public byte[] getPayload() {
        return this.streamerPayload;
    }

    public void printData(String str) {
        Log.e("StreamerHeader", str);
        Log.e("StreamerHeader", "flags: " + Util.byteArrayToHexString(this.flags, true));
        Log.e("StreamerHeader", "seqNum: " + Util.byteArrayToHexString(this.sequenceNumber, true));
        Log.e("StreamerHeader", "prevSeqNum: " + Util.byteArrayToHexString(this.prevSequenceNumber, true));
        Log.e("StreamerHeader", "type: " + Util.byteArrayToHexString(this.type, true));
        Log.e("StreamerHeader", "payloadLen: " + Util.byteArrayToIntLE(this.payloadLength));
        Log.e("StreamerHeader", "streamPayload: " + Util.byteArrayToHexString(this.streamerPayload, true));
    }
}
