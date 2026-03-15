package nano.packets.response;

import java.util.Arrays;
import nano.base.ControlHeader;
/* loaded from: /app/base.apk/classes4.dex */
public class ControlProtocolResponse extends NanoResponse {
    ControlHeader streamerHeader;

    public ControlProtocolResponse(byte[] bArr) {
        super(bArr);
        loadPayloadData();
    }

    @Override // nano.packets.response.NanoResponse
    protected void loadPayloadData() {
        this.payload = Arrays.copyOfRange(getFullData(), 28, 42);
        this.streamerHeader = new ControlHeader(this.payload);
    }

    @Override // nano.packets.response.NanoResponse
    public void printData() {
        super.printData();
        this.streamerHeader.printData("ControlProtocol Control Header");
    }
}
