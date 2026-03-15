package packet.send;

import android.util.Log;
import constants.PacketTypes;
import crypto.SgCrypto;
import packet.MessagePacket;
/* loaded from: /app/base.apk/classes5.dex */
public class OpenChannelMessagePacket extends MessagePacket {
    public byte[] activityId;
    public byte[] channelRequestId;
    public byte[] serviceChannelGuid;
    public byte[] titleId;

    @Override // packet.MessagePacket
    public void loadProtectedData() {
        this.titleId = new byte[]{0, 0, 0, 0};
        this.activityId = new byte[]{0, 0, 0, 0};
        try {
            this.protectedPayload.write(this.channelRequestId);
            this.protectedPayload.write(this.titleId);
            this.protectedPayload.write(this.serviceChannelGuid);
            this.protectedPayload.write(this.activityId);
        } catch (Exception unused) {
            Log.e("ERR", "Error writing packet data");
        }
        this.protectedPayloadRawSize = this.protectedPayload.toByteArray().length;
        this.protectedPayloadPaddedSize = addProtectedPayloadPadding();
    }

    public void setServiceChannelGuid(byte[] bArr) {
        this.serviceChannelGuid = bArr;
    }

    public void setChannelRequestId(byte[] bArr) {
        this.channelRequestId = bArr;
    }

    public OpenChannelMessagePacket(SgCrypto sgCrypto) {
        setCryptoData(sgCrypto);
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.MESSAGE_TYPE;
        this.sequence_number = new byte[]{0, 0, 0, 1};
        this.target_participant_id = new byte[]{0, 0, 0, 0};
        this.flags = new byte[]{-96, 38};
        this.channel_id = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
    }
}
