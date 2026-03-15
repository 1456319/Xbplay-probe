package packet.receive;

import crypto.SgCrypto;
import packet.SimpleResponse;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class ConnectionSimpleResponse extends SimpleResponse {
    public byte[] connectResult;
    public byte[] iv;
    public byte[] pairingState;
    public byte[] participantId;

    public ConnectionSimpleResponse(byte[] bArr, SgCrypto sgCrypto) {
        super(bArr, sgCrypto);
        byte[] bArr2 = sgCrypto.aes_iv;
        this.packet_length_protected = readByteArray(2);
        this.packet_version = readByteArray(2);
        byte[] readByteArray = readByteArray(16);
        this.iv = readByteArray;
        sgCrypto.aes_iv = readByteArray;
        loadProtectedData();
        decryptProtectedData();
        sgCrypto.aes_iv = bArr2;
        this.connectResult = Util.readInputArray(this.protectedDataDecrypted, 0, 2);
        this.pairingState = Util.readInputArray(this.protectedDataDecrypted, 2, 4);
        this.participantId = Util.readInputArray(this.protectedDataDecrypted, 4, 8);
    }
}
