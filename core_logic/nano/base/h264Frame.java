package nano.base;

import android.util.Log;
import com.google.android.gms.cast.MediaError;
import java.io.ByteArrayOutputStream;
import util.Util;
/* compiled from: VideoDecoder.java */
/* loaded from: /app/base.apk/classes4.dex */
class h264Frame {
    int frameId;
    int totalPackets;
    int totalSize;
    int packetsAdded = 0;
    private ByteArrayOutputStream frameData = new ByteArrayOutputStream();

    /* JADX INFO: Access modifiers changed from: package-private */
    public h264Frame(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        this.frameId = Util.byteArrayToIntLE(bArr);
        this.totalPackets = Util.byteArrayToIntLE(bArr2);
        this.totalSize = Util.byteArrayToIntLE(bArr3);
    }

    public boolean isInitFrame() {
        try {
            return this.frameData.toByteArray()[4] == 103;
        } catch (Exception unused) {
            return false;
        }
    }

    public void addFrameData(byte[] bArr) {
        this.packetsAdded++;
        if (bArr != null) {
            try {
                this.frameData.write(bArr);
            } catch (Exception e) {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Error writing data to frame: " + e.getMessage());
            }
        }
    }

    public byte[] getFullFrameData() {
        if (isFrameComplete()) {
            return this.frameData.toByteArray();
        }
        return null;
    }

    private boolean isFrameComplete() {
        return this.totalPackets == this.packetsAdded;
    }
}
