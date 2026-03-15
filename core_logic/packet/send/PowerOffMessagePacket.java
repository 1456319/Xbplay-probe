package packet.send;

import android.util.Log;
import constants.PacketTypes;
import crypto.SgCrypto;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import packet.MessagePacket;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class PowerOffMessagePacket extends MessagePacket {
    public byte[] liveId;

    @Override // packet.MessagePacket
    public void loadProtectedData() {
        writeSgString(this.liveId, this.protectedPayload);
        this.protectedPayloadRawSize = this.protectedPayload.toByteArray().length;
        this.protectedPayloadPaddedSize = addProtectedPayloadPadding();
    }

    public PowerOffMessagePacket(SgCrypto sgCrypto) {
        setCryptoData(sgCrypto);
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.MESSAGE_TYPE;
        this.target_participant_id = new byte[]{0, 0, 0, 0};
        this.flags = new byte[]{-96, 57};
        this.channel_id = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
    }

    public DatagramPacket send(byte[] bArr, InetAddress inetAddress, int i) {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(new DatagramPacket(bArr, bArr.length, inetAddress, i));
            Log.e("packet", "PowerOffPacket: " + Util.byteArrayToHexString(bArr, true));
            datagramSocket.close();
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    public void setLiveId(String str) {
        this.liveId = Util.stringToByteArray(str.substring(3));
    }
}
