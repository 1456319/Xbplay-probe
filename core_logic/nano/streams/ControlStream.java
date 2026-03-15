package nano.streams;

import Interfaces.NanoStreamEvents;
import android.util.Log;
import com.google.android.gms.cast.MediaError;
import com.google.common.base.Ascii;
import nano.base.Nano;
import nano.packets.response.ControlProtocolResponse;
import nano.packets.response.NanoResponse;
import nano.packets.send.ControlProtocol;
/* loaded from: /app/base.apk/classes4.dex */
public class ControlStream extends NanoStream {
    public ControlStream(Nano nano2, NanoStreamEvents nanoStreamEvents) {
        super(nano2, nanoStreamEvents);
        this.channelName = "Microsoft::Rdp::Dct::Channel::Class::Control";
    }

    public void sendControllerInit(int i) {
        send(new ControlProtocol(this.sequenceNumber, this.channelId, new byte[]{Ascii.VT, 0}, new byte[]{1, 0}));
    }

    @Override // nano.streams.NanoStream, nano.streams.StreamListener
    public void receive(NanoResponse nanoResponse) {
        Log.e("ON_RECEIVE_CONTROL", "Printed Below");
        if (nanoResponse.getPacketType() == 35) {
            new ControlProtocolResponse(nanoResponse.getFullData()).printData();
        } else {
            Log.e(MediaError.ERROR_TYPE_ERROR, "Received NON Streamer packet in ControlStream");
        }
    }
}
