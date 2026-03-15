package nano.packets.response;

import android.util.Log;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class InputServerHandshake extends NanoResponse {
    public byte[] height;
    public byte[] initFrameId;
    public byte[] maxTouches;
    public byte[] protocolVersion;
    public byte[] width;

    public InputServerHandshake(byte[] bArr) {
        super(bArr);
        loadPayloadData();
    }

    @Override // nano.packets.response.NanoResponse
    protected void loadPayloadData() {
        this.protocolVersion = readByteArray(4);
        this.width = readByteArray(4);
        this.height = readByteArray(4);
        this.maxTouches = readByteArray(4);
        this.initFrameId = readByteArray(4);
    }

    @Override // nano.packets.response.NanoResponse
    public void printData() {
        super.printData();
        Log.e("InputServerHandshake", "protocolVersion: " + Util.byteArrayToHexString(this.protocolVersion, true));
        Log.e("InputServerHandshake", "width: " + Util.byteArrayToHexString(this.width, true));
        Log.e("InputServerHandshake", "height: " + Util.byteArrayToHexString(this.height, true));
        Log.e("InputServerHandshake", "maxTouches: " + Util.byteArrayToHexString(this.maxTouches, true));
        Log.e("InputServerHandshake", "initFrameId: " + Util.byteArrayToHexString(this.initFrameId, true));
    }
}
