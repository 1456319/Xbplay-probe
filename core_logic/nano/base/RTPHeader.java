package nano.base;

import android.util.Log;
import com.google.android.gms.cast.MediaError;
import java.io.ByteArrayOutputStream;
import java.util.BitSet;
/* loaded from: /app/base.apk/classes4.dex */
public class RTPHeader {
    private byte[] channelId;
    private byte[] connectionId;
    public ByteArrayOutputStream data;
    BitSet header1;
    BitSet header2;
    private byte[] sequenceNumber;
    private byte[] timestamp;

    public RTPHeader(byte[] bArr) {
        if (bArr.length >= 12) {
            this.header1 = BitSet.valueOf(new byte[]{bArr[0]});
            this.header2 = BitSet.valueOf(new byte[]{bArr[1]});
            this.sequenceNumber = new byte[]{bArr[2], bArr[3]};
            this.timestamp = new byte[]{bArr[4], bArr[5], bArr[6], bArr[7]};
            this.connectionId = new byte[]{bArr[8], bArr[9]};
            this.channelId = new byte[]{bArr[10], bArr[11]};
            return;
        }
        Log.e(MediaError.ERROR_TYPE_ERROR, "Invalid RTP header detected!!!");
    }

    public boolean getPadding() {
        return this.header1.get(5);
    }

    public void setPadding(boolean z) {
        this.header1.set(5, z);
    }

    public boolean getMarker() {
        return this.header2.get(7);
    }

    public byte getPayloadType() {
        byte[] byteArray;
        if (getMarker()) {
            BitSet valueOf = BitSet.valueOf(this.header2.toByteArray());
            valueOf.set(7, false);
            byteArray = valueOf.toByteArray();
        } else {
            byteArray = this.header2.toByteArray();
        }
        return byteArray[0];
    }

    public RTPHeader(byte b) {
        this.header1 = new BitSet(8);
        this.header2 = new BitSet(8);
        this.header1.set(7);
        this.header1.set(5);
        this.header2 = BitSet.valueOf(new byte[]{b});
    }

    public ByteArrayOutputStream assemble() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(this.header1.toByteArray());
            byteArrayOutputStream.write(this.header2.toByteArray());
            byteArrayOutputStream.write(this.sequenceNumber);
            byteArrayOutputStream.write(this.timestamp);
            byteArrayOutputStream.write(this.connectionId);
            byteArrayOutputStream.write(this.channelId);
        } catch (Exception unused) {
            Log.e("ERR", "Error writing RTP header data");
        }
        return byteArrayOutputStream;
    }

    public void setSequenceNumber(byte[] bArr) {
        this.sequenceNumber = bArr;
    }

    public void setTimestamp(byte[] bArr) {
        this.timestamp = bArr;
    }

    public void setConnectionId(byte[] bArr) {
        this.connectionId = bArr;
    }

    public void setChannelId(byte[] bArr) {
        this.channelId = bArr;
    }

    public byte[] getChannelId() {
        return this.channelId;
    }
}
