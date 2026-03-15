package nano.streams;

import Interfaces.NanoStreamEvents;
import android.util.Log;
import com.google.android.gms.cast.MediaError;
import nano.base.Nano;
import nano.packets.response.ControlHandshakeResponse;
import nano.packets.response.NanoResponse;
import nano.packets.send.ControlHandshake;
import nano.packets.send.UdpHandshake;
/* loaded from: /app/base.apk/classes4.dex */
public class CoreStream extends NanoStream {
    public boolean connected;

    public CoreStream(Nano nano2, NanoStreamEvents nanoStreamEvents) {
        super(nano2, nanoStreamEvents);
        this.connected = false;
    }

    public void sendControlHandshake() {
        send(new ControlHandshake());
    }

    public void sendUdpHandshake() {
        new Thread() { // from class: nano.streams.CoreStream.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                boolean z = true;
                while (z && !CoreStream.this.connected) {
                    try {
                        CoreStream.this.send(new UdpHandshake(CoreStream.this.sequenceNumber, CoreStream.this.connectionId));
                        sleep(1000L);
                    } catch (Exception e) {
                        Log.e("ERR", "Receive UDP packet ERROR: " + e.getMessage());
                        z = false;
                    }
                }
            }
        }.start();
    }

    @Override // nano.streams.NanoStream, nano.streams.StreamListener
    public void receive(NanoResponse nanoResponse) {
        Log.e("ON RECEIVE", "Control Channel");
        if (nanoResponse.getPacketType() == 96) {
            ControlHandshakeResponse controlHandshakeResponse = new ControlHandshakeResponse(nanoResponse.getFullData());
            this.connectionId = controlHandshakeResponse.connectionId;
            controlHandshakeResponse.printData();
            sendUdpHandshake();
            return;
        }
        Log.e(MediaError.ERROR_TYPE_ERROR, "Invalid CORE Nano Payload Type Detected. Not processing response.");
        nanoResponse.printData();
    }
}
