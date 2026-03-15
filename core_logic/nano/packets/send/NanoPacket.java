package nano.packets.send;

import android.util.Log;
import com.google.android.gms.cast.MediaError;
import constants.PacketProtocol;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import nano.base.RTPHeader;
import nano.base.StreamerHeader;
import org.spongycastle.util.Arrays;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class NanoPacket {
    public byte[] channelId;
    RTPHeader header;
    public String protocolType;
    public byte[] sequenceNumber;
    StreamerHeader streamerHeader;
    protected ByteArrayOutputStream fullPacket = new ByteArrayOutputStream();
    protected ByteArrayOutputStream payload = new ByteArrayOutputStream();

    /* JADX INFO: Access modifiers changed from: package-private */
    public NanoPacket(int i, byte[] bArr) {
        setSequenceNumber(i);
        this.channelId = bArr;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setHeader(RTPHeader rTPHeader) {
        this.header = rTPHeader;
    }

    public void setStreamerHeader(StreamerHeader streamerHeader) {
        this.streamerHeader = streamerHeader;
    }

    public byte getPacketType() {
        return this.header.getPayloadType();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setSequenceNumber(int i) {
        this.sequenceNumber = Util.intLengthTo2Bytes(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public byte[] generateTimestamp() {
        return ByteBuffer.allocate(4).putInt((int) System.currentTimeMillis()).array();
    }

    private ByteArrayOutputStream prependLength(ByteArrayOutputStream byteArrayOutputStream) {
        int length;
        ByteArrayOutputStream byteArrayOutputStream2;
        ByteArrayOutputStream byteArrayOutputStream3 = null;
        try {
            length = getPacket(byteArrayOutputStream).length;
            byteArrayOutputStream2 = new ByteArrayOutputStream();
        } catch (Exception unused) {
        }
        try {
            byteArrayOutputStream2.write(Util.intLengthTo4BytesLE(length));
            byteArrayOutputStream2.write(byteArrayOutputStream.toByteArray());
            return byteArrayOutputStream2;
        } catch (Exception unused2) {
            byteArrayOutputStream3 = byteArrayOutputStream2;
            Log.e(MediaError.ERROR_TYPE_ERROR, "Cannot prepend length to nano packet");
            return byteArrayOutputStream3;
        }
    }

    public byte[] createFullPacket() {
        boolean addPadding;
        this.header.setPadding(getShouldAddPadding(this.payload));
        addPacket(getPacket(this.header.assemble()), this.fullPacket);
        if (getPacketType() == 35 && this.streamerHeader != null) {
            Log.i("SEND PACKET", "SENDING STREAMER DATA");
            if (Util.byteArrayToIntLE(this.streamerHeader.type) != 0) {
                this.streamerHeader.payloadLength = Util.intLengthTo4BytesLE(this.payload.toByteArray().length);
            }
            addPacket(this.streamerHeader.serialize(), this.fullPacket);
            addPadding = addPadding(this.payload, true);
        } else {
            addPadding = addPadding(this.payload, false);
        }
        if (addPadding != this.header.getPadding()) {
            Log.e(MediaError.ERROR_TYPE_ERROR, "RTP HEADER PADDING MISSMATCH!");
        }
        addPacket(getPacket(this.payload), this.fullPacket);
        if (this.protocolType == PacketProtocol.TCP) {
            this.fullPacket = prependLength(this.fullPacket);
        }
        return getPacket(this.fullPacket);
    }

    private boolean getShouldAddPadding(ByteArrayOutputStream byteArrayOutputStream) {
        return byteArrayOutputStream.toByteArray().length % 4 > 0;
    }

    public boolean addPadding(ByteArrayOutputStream byteArrayOutputStream, boolean z) {
        int length = byteArrayOutputStream.toByteArray().length % 4;
        int i = 4 - length;
        if (length != 0) {
            byte[] bArr = new byte[i];
            for (int i2 = 0; i2 < i; i2++) {
                if (i - i2 <= 1) {
                    bArr[i2] = (byte) i;
                } else {
                    bArr[i2] = 0;
                }
            }
            try {
                if (!z) {
                    byteArrayOutputStream.write(bArr);
                } else {
                    byteArrayOutputStream.write(Arrays.reverse(bArr));
                }
            } catch (Exception unused) {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Error writing padding");
            }
            return true;
        }
        return false;
    }

    public void printData(String str) {
        Log.w("NanoPacket: ", str);
        Log.w("NanoPacket: ", "Full Packet: " + Util.byteArrayToHexString(getPacket(this.fullPacket), true));
    }

    public byte[] getPacket(ByteArrayOutputStream byteArrayOutputStream) {
        if (byteArrayOutputStream == null) {
            Log.e("HERE", "output stream empty");
            return new byte[0];
        }
        return byteArrayOutputStream.toByteArray();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void addPacket(byte[] bArr, ByteArrayOutputStream byteArrayOutputStream) {
        try {
            byteArrayOutputStream.write(bArr);
        } catch (Exception unused) {
            Log.e(MediaError.ERROR_TYPE_ERROR, "Cannot write to output stream");
        }
    }
}
