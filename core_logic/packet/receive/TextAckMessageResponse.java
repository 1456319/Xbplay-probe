package packet.receive;

import android.util.Log;
import packet.MessageResponseProtected;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class TextAckMessageResponse extends MessageResponseProtected {
    public byte[] textSessionId;
    public byte[] textVersionAck;

    public TextAckMessageResponse(byte[] bArr) {
        super(bArr);
        this.textSessionId = readByteArray(4);
        this.textVersionAck = readByteArray(4);
    }

    @Override // packet.MessageResponseProtected
    public void printMessageProtectedData() {
        Log.e("LIB", "textSessionId: " + Util.byteArrayToHexString(this.textSessionId, true));
        Log.e("LIB", "textVersionAck: " + Util.byteArrayToHexString(this.textVersionAck, true));
    }
}
