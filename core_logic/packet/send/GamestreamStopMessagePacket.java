package packet.send;

import android.util.Log;
import constants.PacketTypes;
import crypto.SgCrypto;
import packet.MessagePacket;
/* loaded from: /app/base.apk/classes5.dex */
public class GamestreamStopMessagePacket extends MessagePacket {
    public byte[] jsonBody;

    @Override // packet.MessagePacket
    public void loadProtectedData() {
        byte[] loadStopConfig = loadStopConfig();
        this.jsonBody = loadStopConfig;
        try {
            writeSgString(loadStopConfig, this.protectedPayload);
        } catch (Exception unused) {
            Log.e("ERR", "Error writing packet data");
        }
        this.protectedPayloadRawSize = this.protectedPayload.toByteArray().length;
        this.protectedPayloadPaddedSize = addProtectedPayloadPadding();
    }

    private byte[] loadStopConfig() {
        return "{\"type\": 2}".getBytes();
    }

    public GamestreamStopMessagePacket(SgCrypto sgCrypto) {
        setCryptoData(sgCrypto);
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.MESSAGE_TYPE;
        this.sequence_number = new byte[]{0, 0, 0, 1};
        this.target_participant_id = new byte[]{0, 0, 0, 0};
        this.flags = new byte[]{-96, 28};
        this.channel_id = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
    }
}
