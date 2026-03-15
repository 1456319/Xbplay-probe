package packet.receive;

import android.util.Log;
import packet.MessageResponseProtected;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class OpenChannelMessageResponse extends MessageResponseProtected {
    public byte[] channelRequestId;
    public byte[] result;
    public byte[] targetChannelId;

    public OpenChannelMessageResponse(byte[] bArr) {
        super(bArr);
        this.channelRequestId = readByteArray(4);
        this.targetChannelId = readByteArray(8);
        this.result = readByteArray(4);
    }

    @Override // packet.MessageResponseProtected
    public void printMessageProtectedData() {
        Log.e("OpenChannelProtected", "channelRequestId: " + Util.byteArrayToHexString(this.channelRequestId, true));
        Log.e("OpenChannelProtected", "targetChannelId: " + Util.byteArrayToHexString(this.targetChannelId, true));
        Log.e("OpenChannelProtected", "result: " + Util.byteArrayToHexString(this.result, true));
    }
}
