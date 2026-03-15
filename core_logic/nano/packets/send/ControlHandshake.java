package nano.packets.send;

import constants.NanoPayloadTypes;
import constants.PacketProtocol;
import nano.base.RTPHeader;
/* loaded from: /app/base.apk/classes4.dex */
public class ControlHandshake extends NanoPacket {
    byte[] connectionId;
    byte[] type;

    public ControlHandshake() {
        super(0, new byte[]{0, 0});
        this.type = new byte[]{0};
        this.protocolType = PacketProtocol.TCP;
        this.connectionId = generateConnectionId();
        RTPHeader rTPHeader = new RTPHeader((byte) NanoPayloadTypes.CONTROL);
        rTPHeader.setSequenceNumber(this.sequenceNumber);
        rTPHeader.setTimestamp(new byte[]{0, 0, 0, 0});
        rTPHeader.setConnectionId(new byte[]{0, 0});
        rTPHeader.setChannelId(this.channelId);
        setHeader(rTPHeader);
        addPacket(this.type, this.payload);
        addPacket(this.connectionId, this.payload);
    }

    private byte[] generateConnectionId() {
        return new byte[]{-87, -125};
    }
}
