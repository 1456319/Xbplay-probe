package packet;

import android.util.Log;
import com.google.android.gms.cast.MediaError;
import java.util.Arrays;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class MessageResponseProtected {
    private int offset = 0;
    private byte[] payload;

    public MessageResponseProtected(byte[] bArr) {
        this.payload = bArr;
    }

    public byte[] readByteArray(int i) {
        byte[] bArr = this.payload;
        int i2 = this.offset;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i2, i2 + i);
        this.offset += i;
        return copyOfRange;
    }

    public int byteArrayToLenthInt() {
        byte[] bArr = this.payload;
        int i = this.offset;
        byte[] bArr2 = {bArr[i], bArr[i + 1]};
        this.offset = i + 2;
        return Util.unsignedShortToInt(bArr2);
    }

    public byte[] readSgString() {
        int byteArrayToLenthInt = byteArrayToLenthInt();
        byte[] bArr = this.payload;
        int i = this.offset;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i, i + byteArrayToLenthInt);
        this.offset = this.offset + byteArrayToLenthInt + 1;
        return copyOfRange;
    }

    public void printMessageProtectedData() {
        Log.e(MediaError.ERROR_TYPE_ERROR, "Print needs to be created in each child class");
    }
}
