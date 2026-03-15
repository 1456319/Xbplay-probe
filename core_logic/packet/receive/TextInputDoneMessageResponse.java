package packet.receive;

import android.util.Log;
import packet.MessageResponseProtected;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class TextInputDoneMessageResponse extends MessageResponseProtected {
    public byte[] textFlags;
    public byte[] textResult;
    public byte[] textSessionId;
    public byte[] textVersion;

    public TextInputDoneMessageResponse(byte[] bArr) {
        super(bArr);
        this.textSessionId = readByteArray(4);
        this.textVersion = readByteArray(4);
        this.textFlags = readByteArray(4);
        this.textResult = readByteArray(4);
    }

    @Override // packet.MessageResponseProtected
    public void printMessageProtectedData() {
        Log.e("LIB", "textSessionId: " + Util.byteArrayToHexString(this.textSessionId, true));
        Log.e("LIB", "textVersion: " + Util.byteArrayToHexString(this.textVersion, true));
        Log.e("LIB", "textFlags: " + Util.byteArrayToHexString(this.textFlags, true));
        Log.e("LIB", "textResult: " + Util.byteArrayToHexString(this.textResult, true));
    }
}
