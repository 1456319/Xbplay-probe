package nano.streams;

import Interfaces.NanoStreamEvents;
import android.util.Log;
import com.google.android.gms.cast.MediaError;
import constants.VideoControlFlags;
import nano.base.Nano;
import nano.packets.response.NanoResponse;
import nano.packets.response.VideoServerHandshake;
import nano.packets.send.VideoClientHandshake;
import nano.packets.send.VideoControl;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class VideoStream extends NanoStream {
    public VideoStream(Nano nano2, NanoStreamEvents nanoStreamEvents) {
        super(nano2, nanoStreamEvents);
        this.channelName = "Microsoft::Rdp::Dct::Channel::Class::Video";
    }

    private void sendStartVideoStream() {
        send(new VideoControl(this.sequenceNumber, this.channelId, VideoControlFlags.START_STREAM));
    }

    @Override // nano.streams.NanoStream, nano.streams.StreamListener
    public void receive(NanoResponse nanoResponse) {
        Log.e("ON_RECEIVE_VIDEO", "Printed Below");
        if (nanoResponse.getPacketType() == 35) {
            if (nanoResponse.getStreamerHeader().type[0] == 1) {
                Log.i("VideoStream", "Received Streamer of type SERVER HANDSHAKE - Sent client handshake and started the stream");
                VideoServerHandshake videoServerHandshake = new VideoServerHandshake(nanoResponse.getFullData());
                videoServerHandshake.printData();
                this.sequenceNumber = Util.byteArrayToIntLE(videoServerHandshake.getStreamerHeader().sequenceNumber) + 1;
                send(new VideoClientHandshake(1, this.channelId, videoServerHandshake.videoFormats[0], videoServerHandshake.getStreamerHeader()));
                sendStartVideoStream();
                return;
            } else if (nanoResponse.getStreamerHeader().type[0] == 3) {
                Log.e("VideoStream", "Received Streamer of type CONTROL");
                return;
            } else if (nanoResponse.getStreamerHeader().type[0] == 4) {
                Log.e("VideoStream", "Received Streamer of type DATA");
                this.nanoEvents.rawStreamData(this.channelName, nanoResponse);
                return;
            } else {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Received Streamer of unknown type in Video Streamer: " + Util.byteArrayToHexString(nanoResponse.getStreamerHeader().type, true));
                return;
            }
        }
        Log.e(MediaError.ERROR_TYPE_ERROR, "Received NON Streamer packet in video streamer");
    }
}
