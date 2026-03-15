package packet;

import android.util.Log;
import crypto.SgCrypto;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class MessagePacket {
    public byte[] aes_iv;
    public byte[] channel_id;

    /* renamed from: crypto  reason: collision with root package name */
    SgCrypto f28crypto;
    public byte[] encryptKey;
    public byte[] flags;
    public byte[] hashKey;
    public byte[] packet_data;
    public byte[] packet_type;
    public byte[] sequence_number;
    public byte[] source_participant_id;
    public byte[] target_participant_id;
    public int protectedPayloadPaddedSize = 0;
    public int protectedPayloadRawSize = 0;
    public int last_offset = 0;
    public ByteArrayOutputStream payload = new ByteArrayOutputStream();
    public ByteArrayOutputStream fullPacket = new ByteArrayOutputStream();
    public ByteArrayOutputStream protectedPayload = new ByteArrayOutputStream();

    public void loadProtectedData() {
    }

    public byte[] createFullPacket() {
        addFullPacket(this.packet_type);
        addFullPacket(Util.intLengthTo2Bytes(this.protectedPayloadRawSize));
        addFullPacket(this.sequence_number);
        addFullPacket(this.target_participant_id);
        addFullPacket(this.source_participant_id);
        addFullPacket(this.flags);
        addFullPacket(this.channel_id);
        SgCrypto sgCrypto = new SgCrypto();
        ByteArrayOutputStream encryptV2 = sgCrypto.encryptV2(Util.byteArrayToOutputStream(Util.copyHeadBytes(getFullPacket(), 16)), this.aes_iv, Util.createNullByteArray(16));
        Log.i("createFullPacket", "decryptedPayload" + Util.byteArrayToHexString(this.protectedPayload.toByteArray(), true));
        ByteArrayOutputStream encryptV22 = sgCrypto.encryptV2(this.protectedPayload, this.encryptKey, encryptV2.toByteArray());
        this.protectedPayload = encryptV22;
        addFullPacket(encryptV22.toByteArray());
        addFullPacket(getSignHash(getFullPacket()));
        return getFullPacket();
    }

    public void setCryptoData(SgCrypto sgCrypto) {
        this.aes_iv = sgCrypto.aes_iv;
        this.hashKey = sgCrypto.hmac_key;
        this.encryptKey = sgCrypto.aes_key;
        this.f28crypto = sgCrypto;
    }

    public void setChannelId(byte[] bArr) {
        this.channel_id = bArr;
    }

    private byte[] getSignHash(byte[] bArr) {
        byte[] bArr2 = new byte[0];
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(this.hashKey, "HmacSHA256"));
            return mac.doFinal(bArr);
        } catch (Exception unused) {
            return bArr2;
        }
    }

    public int addProtectedPayloadPadding() {
        int length = this.protectedPayload.toByteArray().length % 16;
        int i = 16 - length;
        if (length != 0) {
            byte[] bArr = new byte[i];
            for (int i2 = 0; i2 < i; i2++) {
                bArr[i2] = (byte) i;
            }
            try {
                this.protectedPayload.write(bArr);
            } catch (Exception unused) {
            }
        }
        return this.protectedPayload.toByteArray().length;
    }

    public int writeSgString(byte[] bArr, ByteArrayOutputStream byteArrayOutputStream) {
        byte[] bArr2;
        try {
            byteArrayOutputStream.write(Util.intLengthTo2Bytes(bArr.length));
            if (bArr.length > 0) {
                byteArrayOutputStream.write(bArr);
            }
            bArr2 = new byte[]{0};
        } catch (Exception unused) {
        }
        try {
            byteArrayOutputStream.write(bArr2);
        } catch (Exception unused2) {
            bArr = bArr2;
            bArr2 = bArr;
            return bArr2.length + 2;
        }
        return bArr2.length + 2;
    }

    public int writeSgList(ArrayList<byte[]> arrayList, ByteArrayOutputStream byteArrayOutputStream) {
        try {
            byteArrayOutputStream.write(Util.intLengthTo4Bytes(arrayList.size()));
            arrayList.size();
            for (int i = 0; i < arrayList.size(); i++) {
                byteArrayOutputStream.write(arrayList.get(i));
            }
        } catch (Exception unused) {
        }
        return (arrayList.size() * 2) + 2;
    }

    protected void addPayload(byte[] bArr) {
        try {
            this.payload.write(bArr);
        } catch (Exception unused) {
        }
    }

    protected void addFullPacket(byte[] bArr) {
        try {
            this.fullPacket.write(bArr);
        } catch (Exception unused) {
        }
    }

    public byte[] getFullPacket() {
        ByteArrayOutputStream byteArrayOutputStream = this.fullPacket;
        if (byteArrayOutputStream == null) {
            Log.e("HERE", "fullPacket empty");
            return new byte[0];
        }
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] getPayload() {
        ByteArrayOutputStream byteArrayOutputStream = this.payload;
        if (byteArrayOutputStream == null) {
            Log.e("HERE", "Payload empty");
            return new byte[0];
        }
        return byteArrayOutputStream.toByteArray();
    }

    public void setSequenceNumber(int i) {
        this.sequence_number = Util.intLengthTo4Bytes(i);
    }

    public void setParticipantId(byte[] bArr) {
        this.source_participant_id = bArr;
    }
}
