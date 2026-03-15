package packet;

import android.util.Log;
import crypto.SgCrypto;
import java.util.Arrays;
import packet.receive.ConsoleStatusMessageResponse;
import packet.receive.OpenChannelMessageResponse;
import packet.receive.TextAckMessageResponse;
import packet.receive.TextConfigurationMessageResponse;
import packet.receive.TextInputDoneMessageResponse;
import packet.receive.TextInputMessageResponse;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class MessageResponse {

    /* renamed from: crypto  reason: collision with root package name */
    public SgCrypto f29crypto;
    public byte[] data;
    public byte messageTypeFlag;
    public byte[] protectedDataDecrypted;
    public byte[] protectedDataEncrypted;
    public MessageResponseProtected protectedDataProcessed;
    public boolean ackFlag = false;
    public boolean isFragFlag = false;
    public int last_offset = 0;
    public byte[] packet_type = readByteArray(2);
    public byte[] packet_length_protected = readByteArray(2);
    public byte[] sequence_number = readByteArray(4);
    public byte[] target_participant_id = readByteArray(4);
    public byte[] source_participant_id = readByteArray(4);
    public byte[] flags = readByteArray(2);
    public byte[] channel_id = readByteArray(8);

    public MessageResponse(byte[] bArr, SgCrypto sgCrypto) {
        this.f29crypto = sgCrypto;
        this.data = bArr;
        setFlags(this.flags);
        loadProtectedData();
        decryptProtectedData();
        this.protectedDataProcessed = loadProtectedMessageData(this.messageTypeFlag);
    }

    void setFlags(byte[] bArr) {
        String byteArrayToBinString = Util.byteArrayToBinString(bArr);
        String substring = byteArrayToBinString.substring(2, 3);
        String substring2 = byteArrayToBinString.substring(3, 4);
        if (substring.equals("1")) {
            this.ackFlag = true;
        }
        if (substring2.equals("1")) {
            this.isFragFlag = true;
        }
        this.messageTypeFlag = bArr[1];
    }

    MessageResponseProtected loadProtectedMessageData(byte b) {
        if (b == 1) {
            Log.i("ACK", "Received ACK message from console");
        } else if (b == 28) {
            return new JsonMessageResponse(this.protectedDataDecrypted);
        } else {
            if (b == 30) {
                return new ConsoleStatusMessageResponse(this.protectedDataDecrypted);
            }
            if (b == 39) {
                return new OpenChannelMessageResponse(this.protectedDataDecrypted);
            }
            if (b == 43) {
                return new TextConfigurationMessageResponse(this.protectedDataDecrypted);
            }
            if (b == 44) {
                return new TextInputMessageResponse(this.protectedDataDecrypted);
            }
            if (b == 52) {
                return new TextAckMessageResponse(this.protectedDataDecrypted);
            }
            if (b == 53) {
                return new TextInputDoneMessageResponse(this.protectedDataDecrypted);
            }
            Log.e("===========ERROR==", "Packet Type Not Supported: " + Util.byteArrayToHexString(new byte[]{b}, true));
        }
        return null;
    }

    public void printMessageResponseData(MessageResponseProtected messageResponseProtected) {
        Log.e("START PACKET", "===================================================================");
        printPacketData();
        Log.e("HEADER", "packet_type: " + Util.byteArrayToHexString(this.packet_type, true));
        Log.e("HEADER", "protected_payload_length: " + Util.byteArrayToHexString(this.packet_length_protected, true));
        Log.e("HEADER", "seq_number: " + Util.byteArrayToHexString(this.sequence_number, true));
        Log.e("HEADER", "target_participant_id: " + Util.byteArrayToHexString(this.target_participant_id, true));
        Log.e("HEADER", "source_participant_id: " + Util.byteArrayToHexString(this.source_participant_id, true));
        Log.e("HEADER", "rawFlags: " + Util.byteArrayToHexString(this.flags, true));
        Log.e("HEADER", "    typeFlag: " + Util.byteArrayToHexString(new byte[]{this.messageTypeFlag}, true));
        Log.e("HEADER", "    isFragFlag: " + this.isFragFlag);
        Log.e("HEADER", "    requireAckFlag: " + this.ackFlag);
        Log.e("HEADER", "channel_id: " + Util.byteArrayToHexString(this.channel_id, true));
        Log.i("ResponseData", "protectedDataEncr: " + Util.byteArrayToHexString(this.protectedDataEncrypted, true));
        Log.i("ResponseData", "protectedDataDecr: " + Util.byteArrayToHexString(this.protectedDataDecrypted, true));
        if (messageResponseProtected != null) {
            messageResponseProtected.printMessageProtectedData();
        }
        Log.e("END PACKET", "===================================================================");
    }

    public byte[] readByteArray(int i) {
        byte[] bArr = this.data;
        int i2 = this.last_offset;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i2, i2 + i);
        this.last_offset += i;
        return copyOfRange;
    }

    public int byteArrayToLenthInt() {
        byte[] bArr = this.data;
        int i = this.last_offset;
        byte[] bArr2 = {bArr[i], bArr[i + 1]};
        this.last_offset = i + 2;
        return Util.unsignedShortToInt(bArr2);
    }

    public byte[] readSgString() {
        int byteArrayToLenthInt = byteArrayToLenthInt();
        byte[] bArr = this.data;
        int i = this.last_offset;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i, i + byteArrayToLenthInt);
        this.last_offset = this.last_offset + byteArrayToLenthInt + 1;
        return copyOfRange;
    }

    public void loadProtectedData() {
        byte[] readByteArray = readByteArray(this.data.length - this.last_offset);
        this.protectedDataEncrypted = readByteArray;
        this.protectedDataEncrypted = Util.removeTrailingBytes(readByteArray, 32);
    }

    public void decryptProtectedData() {
        int unsignedShortToInt = Util.unsignedShortToInt(this.packet_length_protected);
        byte[] decryptMessageResponse = this.f29crypto.decryptMessageResponse(this.protectedDataEncrypted, Util.copyHeadBytes(this.data, 16));
        this.protectedDataDecrypted = decryptMessageResponse;
        this.protectedDataDecrypted = Arrays.copyOfRange(decryptMessageResponse, 0, unsignedShortToInt);
    }

    public void printPacketData() {
        Log.e("START MESSAGE PACKET", Util.byteArrayToHexString(this.data, true));
        Log.e("Encrypted", Util.byteArrayToHexString(this.protectedDataEncrypted, true));
        Log.e("Decrypted", Util.byteArrayToHexString(this.protectedDataDecrypted, true));
    }
}
