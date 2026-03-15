package nano.packets.response;

import android.util.Log;
import com.google.android.gms.cast.MediaError;
import constants.NanoChannelControlTypes;
import java.nio.ByteBuffer;
import java.util.Arrays;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class ChannelControlResponse extends NanoResponse {
    public byte[] channelName;
    private int flagLength;
    private byte[] flags;
    public byte[] type;

    public ChannelControlResponse(byte[] bArr) {
        super(bArr);
        loadPayloadData();
    }

    @Override // nano.packets.response.NanoResponse
    protected void loadPayloadData() {
        byte[] readByteArray = readByteArray(4);
        this.type = readByteArray;
        if (Arrays.equals(readByteArray, NanoChannelControlTypes.CREATE)) {
            this.channelName = readByteArray(Util.byteArrayToIntLE(readByteArray(2)));
        } else if (Arrays.equals(this.type, NanoChannelControlTypes.OPEN)) {
            int byteArrayToIntLE = Util.byteArrayToIntLE(readByteArray(4));
            this.flagLength = byteArrayToIntLE;
            if (byteArrayToIntLE == 0) {
                this.flags = new byte[]{0};
            } else {
                this.flags = readByteArray(byteArrayToIntLE);
            }
        } else if (Arrays.equals(this.type, NanoChannelControlTypes.CLOSE)) {
            Log.e(MediaError.ERROR_TYPE_ERROR, "Channel Close packet sent, but not currently handling it...");
        } else {
            Log.e(MediaError.ERROR_TYPE_ERROR, "Channel Control Response Type invalid: " + Util.byteArrayToHexString(this.type, true));
        }
    }

    public byte[] getFormattedFlags() {
        byte[] intLengthTo4BytesLE = Util.intLengthTo4BytesLE(this.flagLength);
        ByteBuffer wrap = ByteBuffer.wrap(new byte[this.flags.length + 4]);
        wrap.put(intLengthTo4BytesLE);
        wrap.put(this.flags);
        return wrap.array();
    }

    @Override // nano.packets.response.NanoResponse
    public void printData() {
        if (Arrays.equals(this.type, NanoChannelControlTypes.CREATE)) {
            Log.e("ChannelControlResponse", "ChannelName: " + Util.hexByteArrayToASCIIString(this.channelName));
        } else if (Arrays.equals(this.type, NanoChannelControlTypes.OPEN)) {
            Log.e("ChannelControlResponse", "Flags: " + Util.byteArrayToHexString(this.flags, true));
        } else if (Arrays.equals(this.type, NanoChannelControlTypes.CLOSE)) {
            Log.e("ChannelControlResponse", "CLOSE channel response");
        }
    }
}
