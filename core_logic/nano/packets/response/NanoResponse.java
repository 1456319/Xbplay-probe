package nano.packets.response;

import android.util.Log;
import nano.base.RTPHeader;
import nano.base.StreamerHeader;
import org.spongycastle.util.Arrays;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class NanoResponse {
    private byte[] fullData;
    private RTPHeader header;
    protected byte[] payload;
    private StreamerHeader streamer;
    private int HEADER_SIZE = 12;
    private int offset = 0;

    protected void loadPayloadData() {
    }

    public NanoResponse(NanoResponse nanoResponse) {
    }

    public NanoResponse(byte[] bArr) {
        this.fullData = bArr;
        this.header = new RTPHeader(bArr);
        setPayload();
        if (getPacketType() == 35) {
            StreamerHeader streamerHeader = new StreamerHeader(this.payload);
            this.streamer = streamerHeader;
            this.payload = streamerHeader.getPayload();
        }
    }

    public byte[] readByteArray(int i) {
        byte[] bArr = this.payload;
        int i2 = this.offset;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i2, i2 + i);
        this.offset += i;
        return copyOfRange;
    }

    public byte[] readByteArrayLE(int i) {
        byte[] bArr = this.payload;
        int i2 = this.offset;
        byte[] reverse = Arrays.reverse(Arrays.copyOfRange(bArr, i2, i2 + i));
        this.offset += i;
        return reverse;
    }

    private void setPayload() {
        int length = this.fullData.length;
        if (!getIsPadded()) {
            this.payload = Arrays.copyOfRange(this.fullData, this.HEADER_SIZE, length);
            return;
        }
        byte[] bArr = this.fullData;
        this.payload = Arrays.copyOfRange(bArr, this.HEADER_SIZE, length - bArr[length - 1]);
    }

    public RTPHeader getHeader() {
        return this.header;
    }

    public StreamerHeader getStreamerHeader() {
        return this.streamer;
    }

    public byte[] getChannelId() {
        return getHeader().getChannelId();
    }

    public byte getPacketType() {
        return this.header.getPayloadType();
    }

    public boolean getIsPadded() {
        return this.header.getPadding();
    }

    public void printData() {
        Log.w("NanoResponse", "RawData: " + Util.byteArrayToHexString(this.fullData, true));
        Log.w("NanoResponse", "PayloadData: " + Util.byteArrayToHexString(this.payload, true));
        Log.w("NanoResponse", "Padded: " + getIsPadded());
        Log.w("NanoResponse", "Type: " + ((int) getPacketType()));
    }

    public byte[] getFullData() {
        return this.fullData;
    }
}
