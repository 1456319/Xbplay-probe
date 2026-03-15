package nano.packets.send;

import constants.NanoPayloadTypes;
import constants.PacketProtocol;
import nano.base.ControlHeader;
import nano.base.RTPHeader;
import nano.base.StreamerHeader;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class ControlProtocol extends NanoPacket {
    public ControlProtocol(int i, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        super(i, bArr);
        this.protocolType = PacketProtocol.TCP;
        RTPHeader rTPHeader = new RTPHeader((byte) NanoPayloadTypes.STREAMER);
        rTPHeader.setSequenceNumber(new byte[]{0, 0});
        rTPHeader.setTimestamp(new byte[]{0, 0, 0, 0});
        rTPHeader.setConnectionId(new byte[]{0, 0});
        rTPHeader.setChannelId(this.channelId);
        setHeader(rTPHeader);
        StreamerHeader streamerHeader = new StreamerHeader();
        streamerHeader.flags = new byte[]{3, 0, 0, 0};
        streamerHeader.sequenceNumber = Util.intLengthTo4BytesLE(i);
        int i2 = i - 1;
        streamerHeader.prevSequenceNumber = Util.intLengthTo4BytesLE(i2);
        streamerHeader.type = new byte[]{0, 0, 0, 0};
        setStreamerHeader(streamerHeader);
        ControlHeader controlHeader = new ControlHeader(bArr2, bArr3, Util.intLengthTo4BytesLE(i2));
        controlHeader.printData("Control Payload: ");
        addPacket(controlHeader.serialize(), this.payload);
    }
}
