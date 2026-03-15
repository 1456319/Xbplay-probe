package packet;

import android.util.Log;
import com.google.android.gms.cast.MediaError;
import crypto.SgCrypto;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class SimplePacket {
    public byte[] aes_iv;

    /* renamed from: crypto  reason: collision with root package name */
    SgCrypto f30crypto;
    public byte[] encryptKey;
    public byte[] hashKey;
    public byte[] packet_data;
    public byte[] packet_type;
    public byte[] packet_version;
    public int protectedPayloadPaddedSize = 0;
    public int protectedPayloadRawSize = 0;
    public int last_offset = 0;
    public ByteArrayOutputStream payload = new ByteArrayOutputStream();
    public ByteArrayOutputStream fullPacket = new ByteArrayOutputStream();
    public ByteArrayOutputStream protectedPayload = null;

    public void loadProtectedData() {
    }

    public byte[] createFullPacket() {
        addFullPacket(this.packet_type);
        addFullPacket(Util.calcPayloadLength(this.packet_data));
        if (this.protectedPayload != null) {
            addFullPacket(Util.intLengthTo2Bytes(this.protectedPayloadRawSize));
        }
        addFullPacket(this.packet_version);
        if (this.protectedPayload != null) {
            addFullPacket(getPayload());
            addFullPacket(this.protectedPayload.toByteArray());
            addFullPacket(getSignHash(getFullPacket()));
        } else {
            addFullPacket(getPayload());
        }
        return getFullPacket();
    }

    public DatagramPacket send(byte[] bArr, InetAddress inetAddress, int i) {
        DatagramSocket datagramSocket;
        DatagramPacket datagramPacket;
        DatagramPacket datagramPacket2 = null;
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);
            datagramSocket.send(new DatagramPacket(bArr, bArr.length, inetAddress, i));
            datagramPacket = new DatagramPacket(new byte[1024], 1024);
        } catch (Exception e) {
            e = e;
        }
        try {
            datagramSocket.receive(datagramPacket);
            Log.e("Received", "SIMPLE PACKET RECEIVED FROM CONSOLE!");
            Log.e("ReceivedData:", "" + Util.byteArrayToHexString(datagramPacket.getData(), true));
            datagramSocket.close();
            return datagramPacket;
        } catch (Exception e2) {
            e = e2;
            datagramPacket2 = datagramPacket;
            Log.e(MediaError.ERROR_TYPE_ERROR, "Error discovering Xbox: " + e);
            return datagramPacket2;
        }
    }

    public void setCryptoData(SgCrypto sgCrypto) {
        this.aes_iv = sgCrypto.aes_iv;
        this.hashKey = sgCrypto.hmac_key;
        this.encryptKey = sgCrypto.aes_key;
        this.f30crypto = sgCrypto;
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
        try {
            byteArrayOutputStream.write(Util.intLengthTo2Bytes(bArr.length));
            if (bArr.length == 0) {
                bArr = new byte[]{0};
            }
            byteArrayOutputStream.write(bArr);
        } catch (Exception unused) {
        }
        return bArr.length + 2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void addPayload(byte[] bArr) {
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
}
