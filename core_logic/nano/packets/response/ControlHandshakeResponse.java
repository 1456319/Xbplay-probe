package nano.packets.response;

import android.util.Log;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class ControlHandshakeResponse extends NanoResponse {
    public byte[] connectionId;
    public byte[] type;

    public ControlHandshakeResponse(byte[] bArr) {
        super(bArr);
        loadPayloadData();
    }

    @Override // nano.packets.response.NanoResponse
    protected void loadPayloadData() {
        this.type = readByteArray(1);
        this.connectionId = readByteArrayLE(2);
    }

    @Override // nano.packets.response.NanoResponse
    public void printData() {
        super.printData();
        Log.e("ControlResponse", "Type (syn/ack): " + Util.byteArrayToHexString(this.type, true));
        Log.e("ControlResponse", "ConnectionId: " + Util.byteArrayToHexString(this.connectionId, true));
    }
}
