package nano.streams;

import nano.packets.response.ChannelControlResponse;
import nano.packets.response.NanoResponse;
/* loaded from: /app/base.apk/classes4.dex */
public interface StreamListener {
    void receive(NanoResponse nanoResponse);

    void receiveControlPacket(ChannelControlResponse channelControlResponse);
}
