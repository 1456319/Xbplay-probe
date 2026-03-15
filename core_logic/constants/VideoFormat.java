package constants;

import android.util.Log;
import com.google.android.gms.cast.MediaError;
import java.io.ByteArrayOutputStream;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class VideoFormat {
    public byte[] blueMask;
    public byte[] bpp;
    public byte[] bytes;
    public byte[] codec;
    public byte[] fps;
    public byte[] greenMask;
    public byte[] height;
    public byte[] redMask;
    public byte[] width;

    public byte[] serialize() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        safeWrite(byteArrayOutputStream, this.fps);
        safeWrite(byteArrayOutputStream, this.width);
        safeWrite(byteArrayOutputStream, this.height);
        safeWrite(byteArrayOutputStream, this.codec);
        safeWrite(byteArrayOutputStream, this.bpp);
        safeWrite(byteArrayOutputStream, this.bytes);
        safeWrite(byteArrayOutputStream, this.redMask);
        safeWrite(byteArrayOutputStream, this.greenMask);
        safeWrite(byteArrayOutputStream, this.blueMask);
        return byteArrayOutputStream.toByteArray();
    }

    public void print(String str) {
        Log.i("VideoFormat", str);
        Log.i("VideoFormat", "fps: " + Util.byteArrayToHexString(this.fps, true));
        Log.i("VideoFormat", "width: " + Util.byteArrayToHexString(this.width, true));
        Log.i("VideoFormat", "height: " + Util.byteArrayToHexString(this.height, true));
        Log.i("VideoFormat", "codec: " + Util.byteArrayToHexString(this.codec, true));
        if (this.codec[0] == 2) {
            Log.i("VideoFormat", "bpp: " + Util.byteArrayToHexString(this.bpp, true));
            Log.i("VideoFormat", "bytes: " + Util.byteArrayToHexString(this.bytes, true));
            Log.i("VideoFormat", "redMask: " + Util.byteArrayToHexString(this.redMask, true));
            Log.i("VideoFormat", "greenMask: " + Util.byteArrayToHexString(this.greenMask, true));
            Log.i("VideoFormat", "blueMask: " + Util.byteArrayToHexString(this.blueMask, true));
        }
    }

    private void safeWrite(ByteArrayOutputStream byteArrayOutputStream, byte[] bArr) {
        if (bArr != null) {
            try {
                byteArrayOutputStream.write(bArr);
            } catch (Exception e) {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Error serializing VideoFormat data: " + e.getMessage());
            }
        }
    }
}
