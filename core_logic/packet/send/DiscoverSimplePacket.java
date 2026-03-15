package packet.send;

import constants.PacketTypes;
import packet.SimplePacket;
/* loaded from: /app/base.apk/classes5.dex */
public class DiscoverSimplePacket extends SimplePacket {
    public byte[] flags;
    public byte[] client_type = {0, 8};
    public byte[] min_version = {0, 0};
    public byte[] max_version = {0, 2};

    public DiscoverSimplePacket() {
        byte[] bArr = {0, 0, 0, 0};
        this.flags = bArr;
        addPayload(bArr);
        addPayload(this.client_type);
        addPayload(this.min_version);
        addPayload(this.max_version);
        this.packet_data = getPayload();
        this.packet_type = PacketTypes.DISCOVERY_TYPE;
        this.packet_version = new byte[]{0, 0};
    }
}
