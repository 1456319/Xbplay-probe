package nano.packets.send;

import constants.NanoChannelControlTypes;
import constants.NanoPayloadTypes;
import constants.PacketProtocol;
import nano.base.RTPHeader;
/* loaded from: /app/base.apk/classes4.dex */
public class ChannelOpen extends NanoPacket {
    protected byte[] flags;
    protected byte[] type;

    public ChannelOpen(int i, byte[] bArr, byte[] bArr2) {
        super(i, bArr);
        this.protocolType = PacketProtocol.TCP;
        this.type = NanoChannelControlTypes.OPEN;
        this.flags = bArr2;
        RTPHeader rTPHeader = new RTPHeader((byte) NanoPayloadTypes.CHANNEL_CONTROL);
        rTPHeader.setSequenceNumber(this.sequenceNumber);
        rTPHeader.setTimestamp(generateTimestamp());
        rTPHeader.setConnectionId(new byte[]{0, 0});
        rTPHeader.setChannelId(this.channelId);
        setHeader(rTPHeader);
        addPacket(this.type, this.payload);
        addPacket(this.flags, this.payload);
    }
}
