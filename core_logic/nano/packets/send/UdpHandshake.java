package nano.packets.send;

import constants.NanoPayloadTypes;
import constants.PacketProtocol;
import nano.base.RTPHeader;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class UdpHandshake extends NanoPacket {
    byte[] type;

    public UdpHandshake(int i, byte[] bArr) {
        super(i, new byte[]{0, 0});
        this.type = new byte[]{1};
        this.protocolType = PacketProtocol.UDP;
        setSequenceNumber(i);
        RTPHeader rTPHeader = new RTPHeader((byte) NanoPayloadTypes.UDP_HANDSHAKE);
        rTPHeader.setSequenceNumber(this.sequenceNumber);
        rTPHeader.setTimestamp(Util.intLengthTo4Bytes(0));
        rTPHeader.setConnectionId(bArr);
        rTPHeader.setChannelId(this.channelId);
        setHeader(rTPHeader);
        addPacket(this.type, this.payload);
    }
}
