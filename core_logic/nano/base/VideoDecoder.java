package nano.base;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import com.google.android.gms.cast.MediaError;
import com.google.firebase.sessions.settings.RemoteSettings;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import nano.packets.response.VideoDataResponse;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class VideoDecoder {
    byte[] currentFrameId;
    MediaFormat format;
    private MediaCodec mCodec;
    TextureView surface;
    byte[] sep = {1, 0, 0, 0};
    public byte[] sps = null;
    public byte[] pps = null;
    int packetsReceivedForCurrentFrame = 0;
    boolean firstInit = false;
    ByteArrayOutputStream frame = new ByteArrayOutputStream();
    Map<String, h264Frame> frameList = new HashMap();

    public void writeFrame(h264Frame h264frame) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "/VideoDecoder.h264"), true);
            fileOutputStream.write(h264frame.getFullFrameData());
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e("VideoDecoder", "Error Saving file" + e.getMessage());
        }
    }

    public void saveFrameToFile(VideoDataResponse videoDataResponse) {
        long byteArrayToIntLE = Util.byteArrayToIntLE(videoDataResponse.frameId);
        String str = "frame" + byteArrayToIntLE;
        h264Frame h264frame = this.frameList.get(str);
        if (h264frame != null) {
            Log.e("VideoDecoder", "Found partial packet to load data into...");
        } else {
            h264frame = new h264Frame(videoDataResponse.frameId, videoDataResponse.packetCount, videoDataResponse.totalSize);
        }
        h264frame.addFrameData(videoDataResponse.encodedVideoData);
        this.frameList.put(str, h264frame);
        if (h264frame.getFullFrameData() != null) {
            Log.e("VideoDecoder", "LOADED FULL FRAME: " + h264frame.packetsAdded + RemoteSettings.FORWARD_SLASH_STRING + h264frame.totalPackets);
            if (this.firstInit) {
                writeFrame(h264frame);
            } else if (!h264frame.isInitFrame()) {
                Log.e("VideoDecoder", "Got a full frame, but we havent received an I frame yet. Dropping.");
            } else {
                this.firstInit = true;
                writeFrame(h264frame);
            }
            this.frameList.remove(Long.valueOf(byteArrayToIntLE));
            return;
        }
        Log.e("VideoDecoder", "LOADED PARTIAL FRAME: " + h264frame.packetsAdded + RemoteSettings.FORWARD_SLASH_STRING + h264frame.totalPackets);
    }

    private boolean hasInitData() {
        return (this.sps == null || this.pps == null) ? false : true;
    }

    public void setSurface(TextureView textureView) {
        this.surface = textureView;
    }

    private void initCodec() {
        try {
            Log.e("VideoDecoder", "POSSIBLY INIT CODEC");
            if (!hasInitData() || this.firstInit) {
                return;
            }
            Log.e("VideoDecoder", "DETECTED INIT PACKET");
            this.mCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat createVideoFormat = MediaFormat.createVideoFormat("video/avc", 1920, 1080);
            this.format = createVideoFormat;
            createVideoFormat.setInteger("max-input-size", 2073600);
            this.format.setInteger("durationUs", 12600000);
            this.format.setInteger("i-frame-interval", 0);
            this.format.setByteBuffer("csd-0", ByteBuffer.wrap(this.sps));
            this.format.setByteBuffer("csd-1", ByteBuffer.wrap(this.pps));
            this.mCodec.configure(this.format, new Surface(this.surface.getSurfaceTexture()), (MediaCrypto) null, 0);
            this.mCodec.start();
            this.firstInit = true;
            run();
        } catch (Exception e) {
            Log.e("VideoDecoder", "Error initializing codec" + e.getMessage());
            this.firstInit = false;
        }
    }

    private void sendFrame() {
        if (this.mCodec != null && this.frame.toByteArray().length > 0) {
            Log.e("VideoDecoder", "Sending Full Frame: " + Util.byteArrayToHexString(this.frame.toByteArray(), true));
            int dequeueInputBuffer = this.mCodec.dequeueInputBuffer(0L);
            if (dequeueInputBuffer >= 0) {
                Log.e("VideoDecoder", "InputIndex is: " + dequeueInputBuffer);
                this.mCodec.getInputBuffer(dequeueInputBuffer).put(this.frame.toByteArray());
                this.mCodec.queueInputBuffer(dequeueInputBuffer, 0, this.frame.toByteArray().length, 0L, 0);
            } else {
                Log.e("VideoDecoder", "Error inputIndex value < 0");
            }
        } else {
            Log.e("VideoDecoder", "Error codec or frame null");
        }
        this.frame = new ByteArrayOutputStream();
        this.packetsReceivedForCurrentFrame = 0;
        this.currentFrameId = new byte[0];
    }

    private boolean isLastPacketInFrame(VideoDataResponse videoDataResponse) {
        return Util.byteArrayToIntLE(videoDataResponse.dataLength) + Util.byteArrayToIntLE(videoDataResponse.packetOffset) == Util.byteArrayToIntLE(videoDataResponse.totalSize);
    }

    private boolean frameIsComplete(VideoDataResponse videoDataResponse) {
        if (this.packetsReceivedForCurrentFrame == Util.byteArrayToIntLE(videoDataResponse.packetCount)) {
            return true;
        }
        Log.e("VidoeDecoder", "Processed: " + this.packetsReceivedForCurrentFrame + " Expected: " + Util.byteArrayToIntLE(videoDataResponse.packetCount));
        return false;
    }

    public void loadFrame(VideoDataResponse videoDataResponse) {
        if (!Arrays.equals(this.currentFrameId, videoDataResponse.frameId)) {
            this.currentFrameId = videoDataResponse.frameId;
            this.packetsReceivedForCurrentFrame = 1;
            this.frame = new ByteArrayOutputStream();
        } else {
            this.packetsReceivedForCurrentFrame++;
        }
        loadFrameData(videoDataResponse);
        if (isLastPacketInFrame(videoDataResponse)) {
            if (frameIsComplete(videoDataResponse)) {
                sendFrame();
                return;
            }
            Log.e("VideoDecoder", "Error loading frame. Missing packet detected...");
            this.frame = new ByteArrayOutputStream();
            this.currentFrameId = videoDataResponse.frameId;
            this.packetsReceivedForCurrentFrame = 1;
        }
    }

    private void printFrameData() {
        Log.e("VideoDecoder", "Video Frame Data");
        Log.e("VideoDecoder", "" + Util.byteArrayToHexString(this.sps, true));
        Log.e("VideoDecoder", "" + Util.byteArrayToHexString(this.pps, true));
        Log.e("VideoDecoder", "" + Util.byteArrayToHexString(this.frame.toByteArray(), true));
    }

    private void loadFrameData(VideoDataResponse videoDataResponse) {
        byte[] bArr = videoDataResponse.encodedVideoData;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean z = false;
        for (int length = bArr.length - 1; length >= 0; length--) {
            byteArrayOutputStream.write(bArr[length]);
            if (byteArrayOutputStream.toByteArray().length >= 4) {
                if (Arrays.equals(this.sep, Arrays.copyOfRange(byteArrayOutputStream.toByteArray(), byteArrayOutputStream.toByteArray().length - 4, byteArrayOutputStream.toByteArray().length))) {
                    z = saveFrameData(byteArrayOutputStream);
                    byteArrayOutputStream = new ByteArrayOutputStream();
                } else if (length == 0) {
                    z = saveFrameData(byteArrayOutputStream);
                    byteArrayOutputStream = new ByteArrayOutputStream();
                }
                if (z) {
                    initCodec();
                }
            }
        }
    }

    private boolean saveFrameData(ByteArrayOutputStream byteArrayOutputStream) {
        byte[] reverse = org.spongycastle.util.Arrays.reverse(byteArrayOutputStream.toByteArray());
        byte[] copyOfRange = Arrays.copyOfRange(reverse, 0, 5);
        if (Arrays.equals(copyOfRange, new byte[]{0, 0, 0, 1, 103})) {
            this.sps = reverse;
        } else if (Arrays.equals(copyOfRange, new byte[]{0, 0, 0, 1, 104})) {
            this.pps = reverse;
        } else if (Arrays.equals(copyOfRange, new byte[]{0, 0, 0, 1, 101})) {
            Log.e("VideoDecoder", "Detected I Frame!!!");
            addDataToFrame(this.sps);
            addDataToFrame(this.pps);
            addDataToFrame(reverse);
            return false;
        } else if (Arrays.equals(copyOfRange, new byte[]{0, 0, 0, 1, 65})) {
            Log.e("VideoDecoder", "Detected P Frame!!!");
            addDataToFrame(reverse);
            return false;
        } else {
            addDataToFrame(reverse);
            return false;
        }
        return true;
    }

    private void addDataToFrame(byte[] bArr) {
        if (bArr != null) {
            try {
                this.frame.write(bArr);
            } catch (Exception e) {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Error writing data to frame: " + e.getMessage());
            }
        }
    }

    public void run() {
        try {
            new Thread() { // from class: nano.base.VideoDecoder.1
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    while (true) {
                        try {
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            if (VideoDecoder.this.mCodec != null) {
                                int dequeueOutputBuffer = VideoDecoder.this.mCodec.dequeueOutputBuffer(bufferInfo, 0L);
                                if (dequeueOutputBuffer >= 0) {
                                    Log.e("VideoDecoder", "OutputIndex is: " + dequeueOutputBuffer);
                                    VideoDecoder.this.mCodec.releaseOutputBuffer(dequeueOutputBuffer, true);
                                } else if (dequeueOutputBuffer == -2) {
                                    Log.e("VideoDecoder", "Cant get output buffer data " + dequeueOutputBuffer);
                                    Log.e("VideoDecoder", "Media Format: " + VideoDecoder.this.mCodec.getOutputFormat());
                                } else {
                                    Log.e("VideoDecoder", "OutputIndex is invalid: " + dequeueOutputBuffer);
                                    Thread.sleep(100L);
                                }
                            } else {
                                Log.e("VideoDecoder", "mCodec is null. Checking in thread");
                                Thread.sleep(100L);
                            }
                        } catch (Exception e) {
                            Log.e("VideoDecoder", "Error in Video Decoder thread" + e.getMessage());
                        }
                    }
                }
            }.start();
        } catch (Exception unused) {
            Log.e("VideoDecoder", "RUN THREAD ENDED Catch in try catch block!!");
        }
    }
}
