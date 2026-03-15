package nano.packets.send;

import android.util.Log;
import constants.NanoPayloadTypes;
import constants.PacketProtocol;
import nano.base.RTPHeader;
import nano.base.StreamerHeader;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class InputClientHandshake extends NanoPacket {
    public byte[] maxTouches;
    public byte[] referenceTimestamp;

    public InputClientHandshake(int i, byte[] bArr) {
        super(i, bArr);
        this.maxTouches = Util.intLengthTo4BytesLE(10);
        this.protocolType = PacketProtocol.TCP;
        RTPHeader rTPHeader = new RTPHeader((byte) NanoPayloadTypes.STREAMER);
        rTPHeader.setSequenceNumber(this.sequenceNumber);
        rTPHeader.setTimestamp(Util.intLengthTo4Bytes(0));
        rTPHeader.setConnectionId(Util.intLengthTo2Bytes(0));
        rTPHeader.setChannelId(this.channelId);
        setHeader(rTPHeader);
        StreamerHeader streamerHeader = new StreamerHeader();
        streamerHeader.flags = new byte[]{3, 0, 0, 0};
        streamerHeader.sequenceNumber = Util.intLengthTo4BytesLE(i);
        streamerHeader.prevSequenceNumber = Util.intLengthTo4BytesLE(i - 1);
        streamerHeader.type = new byte[]{2, 0, 0, 0};
        setStreamerHeader(streamerHeader);
        this.referenceTimestamp = Util.longLengthTo8BytesLE(System.currentTimeMillis());
        addPacket(this.maxTouches, this.payload);
        addPacket(this.referenceTimestamp, this.payload);
    }

    @Override // nano.packets.send.NanoPacket
    public void printData(String str) {
        super.printData(str);
        Log.e("InputClientHandshake: ", "maxTouches: " + Util.byteArrayToHexString(this.maxTouches, true));
        Log.e("InputClientHandshake: ", "referenceTimestamp: " + Util.byteArrayToHexString(this.referenceTimestamp, true));
    }
}
