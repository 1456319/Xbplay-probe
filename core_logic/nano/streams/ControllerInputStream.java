package nano.streams;

import Interfaces.NanoStreamEvents;
import android.util.Log;
import com.google.android.gms.cast.MediaError;
import nano.GameControllerButtonModel;
import nano.base.Nano;
import nano.packets.response.InputServerHandshake;
import nano.packets.response.NanoResponse;
import nano.packets.send.InputClientHandshake;
import nano.packets.send.InputFrame;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class ControllerInputStream extends NanoStream {
    public byte[] initInputFrame;
    public byte[] refTimestamp;

    public ControllerInputStream(Nano nano2, NanoStreamEvents nanoStreamEvents) {
        super(nano2, nanoStreamEvents);
        this.channelName = "Microsoft::Rdp::Dct::Channel::Class::Input";
    }

    public void sendButtonPressed(GameControllerButtonModel gameControllerButtonModel) {
        this.initInputFrame = Util.intLengthTo4BytesLE(Util.byteArrayToIntLE(this.initInputFrame) + 1);
        InputFrame inputFrame = new InputFrame(this.sequenceNumber, this.channelId, this.initInputFrame, this.refTimestamp);
        inputFrame.setButtonModel(gameControllerButtonModel);
        send(inputFrame);
    }

    @Override // nano.streams.NanoStream, nano.streams.StreamListener
    public void receive(NanoResponse nanoResponse) {
        Log.e("ON_RECEIVE_INPUT", "Printed Below");
        nanoResponse.printData();
        if (nanoResponse.getPacketType() == 35) {
            if (nanoResponse.getStreamerHeader().type[0] == 1) {
                Log.i("ControllerInputStream", "Received Server handshake. Sending Client handshake");
                InputServerHandshake inputServerHandshake = new InputServerHandshake(nanoResponse.getFullData());
                inputServerHandshake.printData();
                this.sequenceNumber = Util.byteArrayToIntLE(inputServerHandshake.getStreamerHeader().sequenceNumber) + 1;
                this.initInputFrame = inputServerHandshake.initFrameId;
                InputClientHandshake inputClientHandshake = new InputClientHandshake(this.sequenceNumber, this.channelId);
                inputClientHandshake.printData("ClientHandshake: ");
                send(inputClientHandshake);
                this.refTimestamp = inputClientHandshake.referenceTimestamp;
                return;
            } else if (nanoResponse.getStreamerHeader().type[0] == 3) {
                Log.e("ControllerInputStream", "Received Streamer of type CONTROL");
                return;
            } else if (nanoResponse.getStreamerHeader().type[0] == 4) {
                Log.e("ControllerInputStream", "Received Streamer of type DATA");
                this.nanoEvents.rawStreamData(this.channelName, nanoResponse);
                return;
            } else {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Received Streamer of unknown type in ControllerInputStream: " + Util.byteArrayToHexString(nanoResponse.getStreamerHeader().type, true));
                return;
            }
        }
        Log.e(MediaError.ERROR_TYPE_ERROR, "Received NON Streamer packet in ControllerInputStream");
    }
}
