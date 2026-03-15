package packet.receive;

import android.util.Log;
import packet.MessageResponseProtected;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class TextInputMessageResponse extends MessageResponseProtected {
    public byte[] baseVersion;
    public byte[] deltaLength;
    public byte[] selectionLength;
    public byte[] selectionStart;
    public byte[] submittedVersion;
    public byte[] textChunk;
    public byte[] textChunkByteStart;
    public byte[] textDelta;
    public byte[] textFlags;
    public byte[] textSessionId;
    public byte[] totalTextByteLength;

    public TextInputMessageResponse(byte[] bArr) {
        super(bArr);
        this.textSessionId = readByteArray(4);
        this.baseVersion = readByteArray(4);
        this.submittedVersion = readByteArray(4);
        this.totalTextByteLength = readByteArray(4);
        this.selectionStart = readByteArray(4);
        this.selectionLength = readByteArray(4);
        this.textFlags = readByteArray(2);
        this.textChunkByteStart = readByteArray(4);
        this.textChunk = readSgString();
        this.deltaLength = readByteArray(2);
    }

    @Override // packet.MessageResponseProtected
    public void printMessageProtectedData() {
        Log.e("LIB", "textSessionId: " + Util.byteArrayToHexString(this.textSessionId, true));
        Log.e("LIB", "baseVersion: " + Util.byteArrayToHexString(this.baseVersion, true));
        Log.e("LIB", "submittedVersion: " + Util.byteArrayToHexString(this.submittedVersion, true));
        Log.e("LIB", "totalTextByteLength: " + Util.byteArrayToHexString(this.totalTextByteLength, true));
        Log.e("LIB", "selectionStart: " + Util.byteArrayToHexString(this.selectionStart, true));
        Log.e("LIB", "selectionLength: " + Util.byteArrayToHexString(this.selectionLength, true));
        Log.e("LIB", "textFlags: " + Util.byteArrayToHexString(this.textFlags, true));
        Log.e("LIB", "textChunkByteStart: " + Util.byteArrayToHexString(this.textChunkByteStart, true));
        Log.e("LIB", "textChunk: " + Util.hexByteArrayToASCIIString(this.textChunk));
        Log.e("LIB", "deltaLength: " + Util.byteArrayToHexString(this.deltaLength, true));
        Log.e("LIB", "textDelta: " + Util.hexByteArrayToASCIIString(this.textDelta));
    }
}
