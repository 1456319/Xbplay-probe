package packet.receive;

import android.util.Log;
import com.vungle.warren.model.AdvertisementDBAdapter;
import packet.JsonMessageResponse;
/* loaded from: /app/base.apk/classes5.dex */
public class GamestreamStateJsonResponse extends JsonMessageResponse {
    public boolean isWirelessConnection;
    public String sessionId;
    public int state;
    public int tcpPort;
    public int transmitLinkSpeed;
    public int udpPort;
    public int wirelessChannel;

    public GamestreamStateJsonResponse(byte[] bArr) {
        super(bArr);
        this.state = getJsonIntValue(AdvertisementDBAdapter.AdvertisementColumns.COLUMN_STATE);
        this.sessionId = getJsonStringValue("sessionId");
        this.udpPort = getJsonIntValue("udpPort");
        this.tcpPort = getJsonIntValue("tcpPort");
        this.isWirelessConnection = getJsonBooleanValue("isWirelessConnection");
        this.wirelessChannel = getJsonIntValue("wirelessChannel");
        this.transmitLinkSpeed = getJsonIntValue("transmitLinkSpeed");
    }

    @Override // packet.JsonMessageResponse, packet.MessageResponseProtected
    public void printMessageProtectedData() {
        Log.e("GamestreamEnabledResp", "state: " + this.state);
        Log.e("GamestreamEnabledResp", "sessionId: " + this.sessionId);
        Log.e("GamestreamEnabledResp", "udpPort: " + this.udpPort);
        Log.e("GamestreamEnabledResp", "tcpPort: " + this.tcpPort);
        Log.e("GamestreamEnabledResp", "isWirelessConnection: " + this.isWirelessConnection);
        Log.e("GamestreamEnabledResp", "wirelessChannel: " + this.wirelessChannel);
        Log.e("GamestreamEnabledResp", "transmitLinkSpeed: " + this.transmitLinkSpeed);
    }
}
