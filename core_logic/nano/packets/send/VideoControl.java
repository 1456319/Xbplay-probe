package nano.packets.send;

import constants.NanoPayloadTypes;
import constants.PacketProtocol;
import nano.base.RTPHeader;
import nano.base.StreamerHeader;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class VideoControl extends NanoPacket {
    protected byte[] videoControlFlags;

    public VideoControl(int i, byte[] bArr, byte[] bArr2) {
        super(i, bArr);
        this.protocolType = PacketProtocol.TCP;
        this.videoControlFlags = bArr2;
        RTPHeader rTPHeader = new RTPHeader((byte) NanoPayloadTypes.STREAMER);
        rTPHeader.setSequenceNumber(new byte[]{0, 0});
        rTPHeader.setTimestamp(new byte[]{0, 0, 0, 0});
        rTPHeader.setConnectionId(new byte[]{0, 0});
        rTPHeader.setChannelId(this.channelId);
        setHeader(rTPHeader);
        StreamerHeader streamerHeader = new StreamerHeader();
        streamerHeader.flags = new byte[]{3, 0, 0, 0};
        streamerHeader.sequenceNumber = Util.intLengthTo4BytesLE(2);
        streamerHeader.prevSequenceNumber = Util.intLengthTo4BytesLE(1);
        streamerHeader.type = new byte[]{3, 0, 0, 0};
        setStreamerHeader(streamerHeader);
        addPacket(this.videoControlFlags, this.payload);
    }
}
