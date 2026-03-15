package packet.send;

import constants.PacketTypes;
import crypto.SgCrypto;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import packet.SimplePacket;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class ConnectSimplePacket extends SimplePacket {
    public byte[] authToken;
    public byte[] iv;
    public byte[] pub_key_type;
    public byte[] public_key;
    public byte[] requestGroupEnd;
    public byte[] requestGroupStart;
    public byte[] requestNumber;
    public byte[] sg_uuid;
    public byte[] userHash;

    @Override // packet.SimplePacket
    public void loadProtectedData() {
        byte[] bArr = new byte[0];
        this.userHash = bArr;
        this.authToken = new byte[0];
        this.requestNumber = new byte[]{0, 0, 0, 0};
        this.requestGroupStart = new byte[]{0, 0, 0, 0};
        this.requestGroupEnd = new byte[]{0, 0, 0, 1};
        writeSgString(bArr, this.protectedPayload);
        writeSgString(this.authToken, this.protectedPayload);
        try {
            this.protectedPayload.write(this.requestNumber);
            this.protectedPayload.write(this.requestGroupStart);
            this.protectedPayload.write(this.requestGroupEnd);
        } catch (Exception unused) {
        }
        this.protectedPayloadRawSize = this.protectedPayload.toByteArray().length;
        this.protectedPayloadPaddedSize = addProtectedPayloadPadding();
        this.protectedPayload = new SgCrypto().encryptV2(this.protectedPayload, this.encryptKey, this.aes_iv);
    }

    public ConnectSimplePacket(SgCrypto sgCrypto) {
        setCryptoData(sgCrypto);
        this.sg_uuid = Util.getBytesFromUUID(UUID.randomUUID());
        this.pub_key_type = new byte[]{0, 0};
        this.public_key = sgCrypto.ourGeneratedPublicKey;
        addPayload(this.sg_uuid);
        addPayload(this.pub_key_type);
        addPayload(sgCrypto.ourGeneratedPublicKey);
        addPayload(sgCrypto.aes_iv);
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.CONNECT_TYPE;
        this.packet_version = new byte[]{0, 2};
        this.protectedPayload = new ByteArrayOutputStream();
    }
}
