package packet.send;

import android.util.Log;
import constants.PacketTypes;
import crypto.SgCrypto;
import java.nio.ByteBuffer;
import packet.MessagePacket;
/* loaded from: /app/base.apk/classes5.dex */
public class GamepadMessagePacket extends MessagePacket {
    public byte[] buttons;
    public byte[] leftThumbstickX;
    public byte[] leftThumbstickY;
    public byte[] leftTrigger;
    public byte[] rightThumbstickX;
    public byte[] rightThumbstickY;
    public byte[] rightTrigger;
    public byte[] timestamp;

    @Override // packet.MessagePacket
    public void loadProtectedData() {
        this.timestamp = ByteBuffer.allocate(8).putInt((int) System.currentTimeMillis()).array();
        this.leftTrigger = new byte[]{0, 0, 0, 0};
        this.rightTrigger = new byte[]{0, 0, 0, 0};
        this.leftThumbstickX = new byte[]{0, 0, 0, 0};
        this.leftThumbstickY = new byte[]{0, 0, 0, 0};
        this.rightThumbstickX = new byte[]{0, 0, 0, 0};
        this.rightThumbstickY = new byte[]{0, 0, 0, 0};
        try {
            this.protectedPayload.write(this.timestamp);
            this.protectedPayload.write(this.buttons);
            this.protectedPayload.write(this.leftTrigger);
            this.protectedPayload.write(this.rightTrigger);
            this.protectedPayload.write(this.leftThumbstickX);
            this.protectedPayload.write(this.leftThumbstickY);
            this.protectedPayload.write(this.rightThumbstickX);
            this.protectedPayload.write(this.rightThumbstickY);
        } catch (Exception unused) {
            Log.e("ERR", "Error writing packet data");
        }
        this.protectedPayloadRawSize = this.protectedPayload.toByteArray().length;
        this.protectedPayloadPaddedSize = addProtectedPayloadPadding();
    }

    public void setButtons(byte[] bArr) {
        this.buttons = bArr;
    }

    public void setTargetChannelId(byte[] bArr) {
        this.channel_id = bArr;
    }

    public GamepadMessagePacket(SgCrypto sgCrypto) {
        setCryptoData(sgCrypto);
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.MESSAGE_TYPE;
        this.sequence_number = new byte[]{0, 0, 0, 1};
        this.target_participant_id = new byte[]{0, 0, 0, 0};
        this.flags = new byte[]{-113, 10};
    }
}
