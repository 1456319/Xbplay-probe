package packet.receive;

import packet.SimpleResponse;
/* loaded from: /app/base.apk/classes5.dex */
public class DiscoverySimpleResponse extends SimpleResponse {
    public byte[] UUID;
    public byte[] cert;
    private int certLength;
    public byte[] consoleName;
    public String ip;
    public byte[] lastError;
    public byte[] primaryFlags;
    public byte[] type;

    public DiscoverySimpleResponse(byte[] bArr) {
        super(bArr, null);
        this.packet_version = readByteArray(2);
        this.primaryFlags = readByteArray(4);
        this.type = readByteArray(2);
        this.consoleName = readSgString();
        this.UUID = readSgString();
        this.lastError = readByteArray(4);
        int byteArrayToLenthInt = byteArrayToLenthInt();
        this.certLength = byteArrayToLenthInt;
        this.cert = readByteArray(byteArrayToLenthInt);
    }
}
