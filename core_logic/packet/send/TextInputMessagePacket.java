package packet.send;

import android.util.Log;
import constants.PacketTypes;
import crypto.SgCrypto;
import packet.MessagePacket;
/* loaded from: /app/base.apk/classes5.dex */
public class TextInputMessagePacket extends MessagePacket {
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

    @Override // packet.MessagePacket
    public void loadProtectedData() {
        this.submittedVersion = new byte[]{0, 0, 0, 1};
        this.totalTextByteLength = new byte[]{0, 0, 0, 2};
        this.selectionStart = new byte[]{-1, -1, -1, -1};
        this.selectionLength = new byte[]{-1, -1, -1, -1};
        this.textFlags = new byte[]{0, 0};
        this.textChunkByteStart = new byte[]{0, 0, 0, 1};
        this.textChunk = "xx".getBytes();
        this.deltaLength = new byte[]{0, 0};
        this.textDelta = new byte[]{0, 0};
        try {
            this.protectedPayload.write(this.textSessionId);
            this.protectedPayload.write(this.baseVersion);
            this.protectedPayload.write(this.submittedVersion);
            this.protectedPayload.write(this.totalTextByteLength);
            this.protectedPayload.write(this.selectionStart);
            this.protectedPayload.write(this.selectionLength);
            this.protectedPayload.write(this.textFlags);
            this.protectedPayload.write(this.textChunkByteStart);
            writeSgString(this.textChunk, this.protectedPayload);
            this.protectedPayload.write(this.deltaLength);
            this.protectedPayload.write(this.textDelta);
        } catch (Exception e) {
            Log.e("ERR", "Error writing packet data");
            e.printStackTrace();
        }
        this.protectedPayloadRawSize = this.protectedPayload.toByteArray().length;
        this.protectedPayloadPaddedSize = addProtectedPayloadPadding();
    }

    public TextInputMessagePacket(SgCrypto sgCrypto, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        this.channel_id = bArr;
        this.textSessionId = bArr2;
        this.baseVersion = bArr3;
        setCryptoData(sgCrypto);
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.MESSAGE_TYPE;
        this.sequence_number = new byte[]{0, 0, 0, 1};
        this.target_participant_id = new byte[]{0, 0, 0, 0};
        this.flags = new byte[]{-81, PacketTypes.TEXT_INPUT};
    }
}
