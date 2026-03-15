package packet;

import android.util.Log;
import crypto.SgCrypto;
import java.util.Arrays;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class SimpleResponse {

    /* renamed from: crypto  reason: collision with root package name */
    public SgCrypto f31crypto;
    public byte[] data;
    public byte[] packet_length_protected;
    public byte[] packet_version;
    public byte[] protectedDataDecrypted;
    public byte[] protectedDataEncrypted;
    public int last_offset = 0;
    public byte[] packet_type = readByteArray(2);
    public byte[] packet_length = readByteArray(2);

    public SimpleResponse(byte[] bArr, SgCrypto sgCrypto) {
        this.f31crypto = sgCrypto;
        this.data = bArr;
    }

    public byte[] readByteArray(int i) {
        byte[] bArr = this.data;
        int i2 = this.last_offset;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i2, i2 + i);
        this.last_offset += i;
        return copyOfRange;
    }

    public int byteArrayToLenthInt() {
        byte[] bArr = this.data;
        int i = this.last_offset;
        byte[] bArr2 = {bArr[i], bArr[i + 1]};
        this.last_offset = i + 2;
        return Util.unsignedShortToInt(bArr2);
    }

    public byte[] readSgString() {
        int byteArrayToLenthInt = byteArrayToLenthInt();
        byte[] bArr = this.data;
        int i = this.last_offset;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i, i + byteArrayToLenthInt);
        this.last_offset = this.last_offset + byteArrayToLenthInt + 1;
        return copyOfRange;
    }

    public void loadProtectedData() {
        byte[] readByteArray = readByteArray(this.data.length - this.last_offset);
        this.protectedDataEncrypted = readByteArray;
        this.protectedDataEncrypted = Util.removeTrailingBytes(readByteArray, 32);
    }

    public void decryptProtectedData() {
        int unsignedShortToInt = Util.unsignedShortToInt(this.packet_length_protected);
        byte[] decrypt = this.f31crypto.decrypt(this.protectedDataEncrypted);
        this.protectedDataDecrypted = decrypt;
        this.protectedDataDecrypted = Arrays.copyOfRange(decrypt, 0, unsignedShortToInt);
    }

    public void printPacketData() {
        Log.e("Received SimplePacket", Util.byteArrayToHexString(this.data, true));
        Log.e("Encrypted", "enc" + Util.byteArrayToHexString(this.protectedDataEncrypted, true));
        Log.e("Decrypted", "decry" + Util.byteArrayToHexString(this.protectedDataDecrypted, true));
    }
}
