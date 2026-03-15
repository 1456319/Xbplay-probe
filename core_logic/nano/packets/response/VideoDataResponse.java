package nano.packets.response;

import android.util.Log;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class VideoDataResponse extends NanoResponse {
    public byte[] dataLength;
    public byte[] encodedVideoData;
    public byte[] frameId;
    public byte[] packetCount;
    public byte[] packetFlags;
    public byte[] packetOffset;
    public byte[] timestamp;
    public byte[] totalSize;

    public VideoDataResponse(byte[] bArr) {
        super(bArr);
        loadPayloadData();
    }

    @Override // nano.packets.response.NanoResponse
    protected void loadPayloadData() {
        this.packetFlags = readByteArray(4);
        this.frameId = readByteArray(4);
        this.timestamp = readByteArray(8);
        this.totalSize = readByteArray(4);
        this.packetCount = readByteArray(4);
        this.packetOffset = readByteArray(4);
        byte[] readByteArray = readByteArray(4);
        this.dataLength = readByteArray;
        this.encodedVideoData = readByteArray(Util.byteArrayToIntLE(readByteArray));
    }

    @Override // nano.packets.response.NanoResponse
    public void printData() {
        super.printData();
        Log.i("VideoDataPacket", "packetFlags: " + Util.byteArrayToHexString(this.packetFlags, true));
        Log.i("VideoDataPacket", "frameId: " + Util.byteArrayToHexString(this.frameId, true));
        Log.i("VideoDataPacket", "timestamp: " + Util.byteArrayToHexString(this.timestamp, true));
        Log.i("VideoDataPacket", "totalSize: " + Util.byteArrayToHexString(this.totalSize, true) + " (dec) " + Util.byteArrayToIntLE(this.totalSize));
        Log.i("VideoDataPacket", "packetCount: " + Util.byteArrayToHexString(this.packetCount, true) + " (dec) " + Util.byteArrayToIntLE(this.packetCount));
        Log.i("VideoDataPacket", "packetOffset: " + Util.byteArrayToHexString(this.packetOffset, true) + " (dec) " + Util.byteArrayToIntLE(this.packetOffset));
        Log.i("VideoDataPacket", "dataLength: " + Util.byteArrayToHexString(this.dataLength, true) + " (dec) " + Util.byteArrayToIntLE(this.dataLength));
        Log.i("VideoDataPacket", "data: " + Util.byteArrayToHexString(this.encodedVideoData, true));
    }
}
