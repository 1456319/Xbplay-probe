package nano.packets.send;

import constants.NanoPayloadTypes;
import constants.PacketProtocol;
import constants.VideoFormat;
import nano.base.RTPHeader;
import nano.base.StreamerHeader;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class VideoClientHandshake extends NanoPacket {
    byte[] initialFrameId;
    VideoFormat videoFormat;

    public VideoClientHandshake(int i, byte[] bArr, VideoFormat videoFormat, StreamerHeader streamerHeader) {
        super(i, bArr);
        this.initialFrameId = Util.intLengthTo4Bytes((int) (Math.random() * 500.0d));
        this.protocolType = PacketProtocol.TCP;
        this.videoFormat = videoFormat;
        RTPHeader rTPHeader = new RTPHeader((byte) NanoPayloadTypes.STREAMER);
        rTPHeader.setSequenceNumber(this.sequenceNumber);
        rTPHeader.setTimestamp(Util.intLengthTo4Bytes(0));
        rTPHeader.setConnectionId(Util.intLengthTo2Bytes(0));
        rTPHeader.setChannelId(this.channelId);
        setHeader(rTPHeader);
        StreamerHeader streamerHeader2 = new StreamerHeader();
        streamerHeader2.flags = new byte[]{3, 0, 0, 0};
        streamerHeader2.sequenceNumber = Util.intLengthTo4BytesLE(i);
        streamerHeader2.prevSequenceNumber = Util.intLengthTo4BytesLE(i - 1);
        streamerHeader2.type = new byte[]{2, 0, 0, 0};
        setStreamerHeader(streamerHeader2);
        addPacket(this.initialFrameId, this.payload);
        addPacket(this.videoFormat.serialize(), this.payload);
    }
}
