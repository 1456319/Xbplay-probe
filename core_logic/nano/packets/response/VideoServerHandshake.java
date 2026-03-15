package nano.packets.response;

import android.util.Log;
import constants.VideoFormat;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class VideoServerHandshake extends NanoResponse {
    public byte[] formatsLength;
    public byte[] fps;
    public byte[] height;
    public byte[] protocolVersion;
    public byte[] refTimestamp;
    public VideoFormat[] videoFormats;
    public byte[] width;

    public VideoServerHandshake(byte[] bArr) {
        super(bArr);
        loadPayloadData();
    }

    @Override // nano.packets.response.NanoResponse
    protected void loadPayloadData() {
        this.protocolVersion = readByteArray(4);
        this.width = readByteArray(4);
        this.height = readByteArray(4);
        this.fps = readByteArray(4);
        this.refTimestamp = readByteArray(8);
        byte[] readByteArray = readByteArray(4);
        this.formatsLength = readByteArray;
        int byteArrayToIntLE = Util.byteArrayToIntLE(readByteArray);
        this.videoFormats = new VideoFormat[byteArrayToIntLE];
        for (int i = 0; i < byteArrayToIntLE; i++) {
            this.videoFormats[i] = readVideoFormat();
        }
    }

    private VideoFormat readVideoFormat() {
        VideoFormat videoFormat = new VideoFormat();
        videoFormat.fps = readByteArray(4);
        videoFormat.width = readByteArray(4);
        videoFormat.height = readByteArray(4);
        videoFormat.codec = readByteArray(4);
        if (videoFormat.codec[0] == 2) {
            videoFormat.bpp = readByteArray(4);
            videoFormat.bytes = readByteArray(4);
            videoFormat.redMask = readByteArray(8);
            videoFormat.greenMask = readByteArray(8);
            videoFormat.blueMask = readByteArray(8);
        }
        return videoFormat;
    }

    @Override // nano.packets.response.NanoResponse
    public void printData() {
        super.printData();
        Log.e("VideoServerHandshake", "protocolVersion: " + Util.byteArrayToHexString(this.protocolVersion, true));
        Log.e("VideoServerHandshake", "width: " + Util.byteArrayToHexString(this.width, true));
        Log.e("VideoServerHandshake", "height: " + Util.byteArrayToHexString(this.height, true));
        Log.e("VideoServerHandshake", "fps: " + Util.byteArrayToHexString(this.fps, true));
        Log.e("VideoServerHandshake", "refTimestamp: " + Util.byteArrayToHexString(this.refTimestamp, true));
        Log.e("VideoServerHandshake", "formatsLength: " + Util.byteArrayToHexString(this.formatsLength, true));
        int i = 0;
        while (true) {
            VideoFormat[] videoFormatArr = this.videoFormats;
            if (i >= videoFormatArr.length) {
                return;
            }
            videoFormatArr[i].print("Video Format: " + i);
            i++;
        }
    }
}
