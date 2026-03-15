package packet.receive;

import android.util.Log;
import com.tapjoy.TJAdUnitConstants;
import packet.JsonMessageResponse;
/* loaded from: /app/base.apk/classes5.dex */
public class GamestreamEnabledJsonResponse extends JsonMessageResponse {
    public boolean enabled;

    public GamestreamEnabledJsonResponse(byte[] bArr) {
        super(bArr);
        this.enabled = getJsonBooleanValue(TJAdUnitConstants.String.ENABLED);
    }

    @Override // packet.JsonMessageResponse, packet.MessageResponseProtected
    public void printMessageProtectedData() {
        Log.e("GamestreamEnabledResp", "enabled: " + this.enabled);
    }
}
