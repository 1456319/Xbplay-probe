package packet.receive;

import android.util.Log;
import packet.MessageResponseProtected;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class TextConfigurationMessageResponse extends MessageResponseProtected {
    public byte[] inputScope;
    public byte[] local;
    public byte[] maxTextLength;
    public byte[] prompt;
    public byte[] textBufferVersion;
    public byte[] textOptions;
    public byte[] textSessionId;

    public TextConfigurationMessageResponse(byte[] bArr) {
        super(bArr);
        this.textSessionId = readByteArray(8);
        this.textBufferVersion = readByteArray(4);
        this.textOptions = readByteArray(4);
        this.inputScope = readByteArray(4);
        this.maxTextLength = readByteArray(4);
        this.local = readSgString();
        this.prompt = readSgString();
    }

    @Override // packet.MessageResponseProtected
    public void printMessageProtectedData() {
        Log.e("LIB", "textSessionId: " + Util.byteArrayToHexString(this.textSessionId, true));
        Log.e("LIB", "textBufferVersion: " + Util.byteArrayToHexString(this.textBufferVersion, true));
        Log.e("LIB", "textOptions: " + Util.byteArrayToHexString(this.textOptions, true));
        Log.e("LIB", "inputScope: " + Util.byteArrayToHexString(this.inputScope, true));
        Log.e("LIB", "maxTextLength: " + Util.byteArrayToHexString(this.maxTextLength, true));
        Log.e("LIB", "local: " + Util.hexByteArrayToASCIIString(this.local));
        Log.e("LIB", "prompt: " + Util.hexByteArrayToASCIIString(this.prompt));
    }
}
