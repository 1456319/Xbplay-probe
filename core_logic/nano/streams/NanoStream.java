package nano.streams;

import Interfaces.NanoStreamEvents;
import android.util.Log;
import com.google.android.gms.cast.MediaError;
import constants.NanoChannelControlTypes;
import java.util.Arrays;
import nano.base.Nano;
import nano.packets.response.ChannelControlResponse;
import nano.packets.response.NanoResponse;
import nano.packets.send.ChannelOpen;
import nano.packets.send.NanoPacket;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class NanoStream implements StreamListener {
    protected byte[] channelId;
    protected String channelName;
    protected byte[] connectionId;

    /* renamed from: nano  reason: collision with root package name */
    protected Nano f18nano;
    protected NanoStreamEvents nanoEvents;
    protected boolean isActive = false;
    protected int sequenceNumber = 1;

    /* JADX INFO: Access modifiers changed from: package-private */
    public NanoStream(Nano nano2, NanoStreamEvents nanoStreamEvents) {
        this.f18nano = nano2;
        this.nanoEvents = nanoStreamEvents;
    }

    protected void sendChannelOpen(byte[] bArr) {
        send(new ChannelOpen(this.sequenceNumber, this.channelId, bArr));
    }

    public byte[] getConnectionId() {
        return this.connectionId;
    }

    public void setConnectionId(byte[] bArr) {
        this.connectionId = bArr;
    }

    public void setActive() {
        this.isActive = true;
    }

    public void setChannelId(byte[] bArr) {
        this.channelId = bArr;
    }

    public byte[] getChannelId() {
        return this.channelId;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void send(NanoPacket nanoPacket) {
        Nano nano2;
        if (this.isActive && (nano2 = this.f18nano) != null) {
            nano2.send(nanoPacket);
            this.sequenceNumber++;
            return;
        }
        Log.e(MediaError.ERROR_TYPE_ERROR, "Send called before stream set to active!");
    }

    @Override // nano.streams.StreamListener
    public void receive(NanoResponse nanoResponse) {
        Log.e(MediaError.ERROR_TYPE_ERROR, "NanoStream base class catch packet. Inheriting class should implement this.");
    }

    @Override // nano.streams.StreamListener
    public void receiveControlPacket(ChannelControlResponse channelControlResponse) {
        if (Arrays.equals(channelControlResponse.type, NanoChannelControlTypes.CREATE) && this.channelName.equals(Util.hexByteArrayToASCIIString(channelControlResponse.channelName))) {
            this.channelId = channelControlResponse.getChannelId();
            this.isActive = true;
            Log.e("CREATE_STREAM", "New Channel: " + this.channelName + " Channel ID: " + Util.byteArrayToHexString(getChannelId(), true));
        } else if (Arrays.equals(this.channelId, channelControlResponse.getChannelId())) {
            if (Arrays.equals(channelControlResponse.type, NanoChannelControlTypes.OPEN)) {
                Log.e("OPEN_STEAM", "Opening Channel: " + this.channelName);
                sendChannelOpen(channelControlResponse.getFormattedFlags());
            } else if (Arrays.equals(channelControlResponse.type, NanoChannelControlTypes.CLOSE)) {
                Log.e("CLOSE_STREAM", "Closing Channel: " + this.channelName);
                this.isActive = false;
            } else {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Invalid Nano channel control response");
            }
        }
    }
}
