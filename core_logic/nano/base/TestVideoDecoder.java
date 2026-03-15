package nano.base;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
/* loaded from: /app/base.apk/classes4.dex */
public class TestVideoDecoder {
    private static final int FRAME_RATE = 30;
    private static final int IFRAME_INTERVAL = 5;
    private static final String MIME_TYPE = "video/avc";
    private static final String TAG = "VideoDecoder2";
    private static final boolean VERBOSE = false;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private MediaCodec mEncoder;
    private Surface mInputSurface;
    private MediaMuxer mMuxer;
    private boolean mMuxerStarted;
    private int mTrackIndex;

    public TestVideoDecoder(int i, int i2, int i3, File file) throws IOException {
        MediaFormat createVideoFormat = MediaFormat.createVideoFormat("video/avc", i, i2);
        createVideoFormat.setInteger("color-format", 2130708361);
        createVideoFormat.setInteger("bitrate", i3);
        createVideoFormat.setInteger("frame-rate", 30);
        createVideoFormat.setInteger("i-frame-interval", 5);
        MediaCodec createEncoderByType = MediaCodec.createEncoderByType("video/avc");
        this.mEncoder = createEncoderByType;
        createEncoderByType.configure(createVideoFormat, (Surface) null, (MediaCrypto) null, 1);
        this.mInputSurface = this.mEncoder.createInputSurface();
        this.mEncoder.start();
        this.mMuxer = new MediaMuxer(file.toString(), 0);
        this.mTrackIndex = -1;
        this.mMuxerStarted = false;
    }

    public Surface getInputSurface() {
        return this.mInputSurface;
    }

    public void release() {
        MediaCodec mediaCodec = this.mEncoder;
        if (mediaCodec != null) {
            mediaCodec.stop();
            this.mEncoder.release();
            this.mEncoder = null;
        }
        MediaMuxer mediaMuxer = this.mMuxer;
        if (mediaMuxer != null) {
            mediaMuxer.stop();
            this.mMuxer.release();
            this.mMuxer = null;
        }
    }

    public void drainEncoder(boolean z) {
        if (z) {
            this.mEncoder.signalEndOfInputStream();
        }
        ByteBuffer[] outputBuffers = this.mEncoder.getOutputBuffers();
        while (true) {
            int dequeueOutputBuffer = this.mEncoder.dequeueOutputBuffer(this.mBufferInfo, 10000L);
            if (dequeueOutputBuffer == -1) {
                if (!z) {
                    return;
                }
            } else if (dequeueOutputBuffer == -3) {
                outputBuffers = this.mEncoder.getOutputBuffers();
            } else if (dequeueOutputBuffer == -2) {
                if (this.mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat outputFormat = this.mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + outputFormat);
                this.mTrackIndex = this.mMuxer.addTrack(outputFormat);
                this.mMuxer.start();
                this.mMuxerStarted = true;
            } else if (dequeueOutputBuffer < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + dequeueOutputBuffer);
            } else {
                ByteBuffer byteBuffer = outputBuffers[dequeueOutputBuffer];
                if (byteBuffer == null) {
                    throw new RuntimeException("encoderOutputBuffer " + dequeueOutputBuffer + " was null");
                }
                if ((this.mBufferInfo.flags & 2) != 0) {
                    this.mBufferInfo.size = 0;
                }
                if (this.mBufferInfo.size != 0) {
                    if (!this.mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    byteBuffer.position(this.mBufferInfo.offset);
                    byteBuffer.limit(this.mBufferInfo.offset + this.mBufferInfo.size);
                    this.mMuxer.writeSampleData(this.mTrackIndex, byteBuffer, this.mBufferInfo);
                }
                this.mEncoder.releaseOutputBuffer(dequeueOutputBuffer, false);
                if ((this.mBufferInfo.flags & 4) != 0) {
                    if (z) {
                        return;
                    }
                    Log.w(TAG, "reached end of stream unexpectedly");
                    return;
                }
            }
        }
    }
}
