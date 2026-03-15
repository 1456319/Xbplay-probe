package packet.send;

import com.google.common.base.Ascii;
import constants.NanoPayloadTypes;
import constants.PacketTypes;
import crypto.SgCrypto;
import packet.MessagePacket;
/* loaded from: /app/base.apk/classes5.dex */
public class LocalJoinMessagePacket extends MessagePacket {
    public byte[] clientVersion;
    public byte[] deviceCapablities;
    public byte[] deviceType;
    public byte[] displayName;
    public byte[] dpiX;
    public byte[] dpiY;
    public byte[] nativeHeight;
    public byte[] nativeWidth;
    public byte[] osMajorVersion;
    public byte[] osMinorVersion;

    @Override // packet.MessagePacket
    public void loadProtectedData() {
        this.deviceType = new byte[]{0, 8};
        this.nativeWidth = new byte[]{4, 56};
        this.nativeHeight = new byte[]{7, Byte.MIN_VALUE};
        this.dpiX = new byte[]{0, NanoPayloadTypes.CONTROL};
        this.dpiY = new byte[]{0, NanoPayloadTypes.CONTROL};
        this.deviceCapablities = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1};
        this.clientVersion = new byte[]{0, 0, 0, Ascii.SO};
        this.osMajorVersion = new byte[]{0, 0, 0, Ascii.SYN};
        this.osMinorVersion = new byte[]{0, 0, 0, 0};
        this.displayName = new byte[]{-127, -35, -63, -46, -56};
        try {
            this.protectedPayload.write(this.deviceType);
            this.protectedPayload.write(this.nativeWidth);
            this.protectedPayload.write(this.nativeHeight);
            this.protectedPayload.write(this.dpiX);
            this.protectedPayload.write(this.dpiY);
            this.protectedPayload.write(this.deviceCapablities);
            this.protectedPayload.write(this.clientVersion);
            this.protectedPayload.write(this.osMajorVersion);
            this.protectedPayload.write(this.osMinorVersion);
        } catch (Exception unused) {
        }
        writeSgString(this.displayName, this.protectedPayload);
        this.protectedPayloadRawSize = this.protectedPayload.toByteArray().length;
        this.protectedPayloadPaddedSize = addProtectedPayloadPadding();
    }

    public LocalJoinMessagePacket(SgCrypto sgCrypto) {
        setCryptoData(sgCrypto);
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.MESSAGE_TYPE;
        this.sequence_number = new byte[]{0, 0, 0, 1};
        this.target_participant_id = new byte[]{0, 0, 0, 0};
        this.flags = new byte[]{32, 3};
        this.channel_id = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
    }
}
