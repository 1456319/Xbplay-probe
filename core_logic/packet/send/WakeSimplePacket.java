package packet.send;

import android.util.Log;
import constants.PacketTypes;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import packet.SimplePacket;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class WakeSimplePacket extends SimplePacket {
    public byte[] liveId;

    public WakeSimplePacket(String str) {
        byte[] stringToByteArray = Util.stringToByteArray(str.substring(3));
        this.liveId = stringToByteArray;
        Log.e("HERE: Live byte", Util.byteArrayToHexString(stringToByteArray, true));
        writeSgString(this.liveId, this.payload);
        addPayload(new byte[]{0});
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.WAKE_TYPE;
        this.packet_version = new byte[]{0, 0};
    }

    @Override // packet.SimplePacket
    public DatagramPacket send(byte[] bArr, InetAddress inetAddress, int i) {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(new DatagramPacket(bArr, bArr.length, InetAddress.getByName("255.255.255.255"), 5050));
            Log.e("packet", "PowerOnPacket: " + Util.byteArrayToHexString(bArr, true));
            datagramSocket.close();
            return null;
        } catch (Exception unused) {
            return null;
        }
    }
}
