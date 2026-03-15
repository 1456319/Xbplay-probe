package nano.packets.send;

import android.util.Log;
import constants.NanoPayloadTypes;
import constants.PacketProtocol;
import nano.GameControllerButtonModel;
import nano.base.RTPHeader;
import nano.base.StreamerHeader;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class InputFrame extends NanoPacket {
    byte[] createdTimestamp;
    byte[] frameId;
    byte[] inputAnalogModel;
    byte[] inputButtonModel;
    byte[] inputExtensionModel;
    byte[] timestamp;

    public InputFrame(int i, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        super(i, bArr);
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
        streamerHeader.type = new byte[]{4, 0, 0, 0};
        setStreamerHeader(streamerHeader);
        this.frameId = bArr2;
        this.createdTimestamp = calcTimestamp(bArr3);
        this.timestamp = calcTimestamp(bArr3);
        addPacket(this.frameId, this.payload);
        addPacket(this.timestamp, this.payload);
        addPacket(this.createdTimestamp, this.payload);
    }

    public void setButtonModel(GameControllerButtonModel gameControllerButtonModel) {
        this.inputButtonModel = gameControllerButtonModel.getButtonModel();
        this.inputAnalogModel = gameControllerButtonModel.getAnalogModel();
        this.inputExtensionModel = gameControllerButtonModel.getExtensionModel();
        addPacket(this.inputButtonModel, this.payload);
        addPacket(this.inputAnalogModel, this.payload);
        addPacket(this.inputExtensionModel, this.payload);
    }

    private byte[] calcTimestamp(byte[] bArr) {
        int currentTimeMillis = ((int) System.currentTimeMillis()) - Util.byteArrayToIntLE(bArr);
        Log.e("InputFrame", "" + currentTimeMillis);
        return Util.longLengthTo8BytesLE(currentTimeMillis);
    }

    @Override // nano.packets.send.NanoPacket
    public void printData(String str) {
        super.printData(str);
        Log.e("InputFrame: ", "frameId: " + Util.byteArrayToHexString(this.frameId, true));
        Log.e("InputFrame: ", "timestamp: " + Util.byteArrayToHexString(this.timestamp, true));
        Log.e("InputFrame: ", "createdTimestamp: " + Util.byteArrayToHexString(this.createdTimestamp, true));
        Log.e("InputFrame: ", "inputButtonModel: " + Util.byteArrayToHexString(this.inputButtonModel, true));
        Log.e("InputFrame: ", "inputAnalogModel: " + Util.byteArrayToHexString(this.inputAnalogModel, true));
        Log.e("InputFrame: ", "inputExtensionModel: " + Util.byteArrayToHexString(this.inputExtensionModel, true));
    }
}
