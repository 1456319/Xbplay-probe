package packet.send;

import constants.PacketTypes;
import crypto.SgCrypto;
import java.util.ArrayList;
import packet.MessagePacket;
/* loaded from: /app/base.apk/classes5.dex */
public class AckMessagePacket extends MessagePacket {
    public byte[] lowWatermark;
    public ArrayList<byte[]> processedList;
    public ArrayList<byte[]> rejectedList;

    @Override // packet.MessagePacket
    public void loadProtectedData() {
        try {
            this.protectedPayload.write(this.lowWatermark);
            writeSgList(this.processedList, this.protectedPayload);
            writeSgList(this.rejectedList, this.protectedPayload);
        } catch (Exception unused) {
        }
        this.protectedPayloadRawSize = this.protectedPayload.toByteArray().length;
        this.protectedPayloadPaddedSize = addProtectedPayloadPadding();
    }

    public void setLowWatermark(byte[] bArr) {
        this.lowWatermark = bArr;
    }

    public void addProcessedList(byte[] bArr) {
        this.processedList.add(bArr);
    }

    public AckMessagePacket(SgCrypto sgCrypto) {
        setCryptoData(sgCrypto);
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.MESSAGE_TYPE;
        this.sequence_number = new byte[]{0, 0, 0, 1};
        this.target_participant_id = new byte[]{0, 0, 0, 0};
        this.flags = new byte[]{Byte.MIN_VALUE, 1};
        this.channel_id = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        this.processedList = new ArrayList<>();
        this.rejectedList = new ArrayList<>();
    }
}
