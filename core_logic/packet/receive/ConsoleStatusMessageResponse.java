package packet.receive;

import android.util.Log;
import packet.MessageResponseProtected;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class ConsoleStatusMessageResponse extends MessageResponseProtected {
    public byte[] activeTitleCount;
    public activeTitle[] activeTitles;
    public byte[] buildNumber;
    public byte[] liveTvProvider;
    public byte[] local;
    public byte[] majorVersion;
    public byte[] minorVersion;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /app/base.apk/classes5.dex */
    public class activeTitle {
        public byte[] aumId;
        public byte[] productId;
        public byte[] sandboxId;
        public byte[] titleDeposition;
        public byte[] titleId;

        private activeTitle() {
        }
    }

    public ConsoleStatusMessageResponse(byte[] bArr) {
        super(bArr);
        this.liveTvProvider = readByteArray(4);
        this.majorVersion = readByteArray(4);
        this.minorVersion = readByteArray(4);
        this.buildNumber = readByteArray(4);
        this.local = readSgString();
        this.activeTitleCount = readByteArray(2);
        readActiveTitles();
    }

    private void readActiveTitles() {
        int unsignedShortToInt = Util.unsignedShortToInt(this.activeTitleCount);
        this.activeTitles = new activeTitle[unsignedShortToInt];
        for (int i = 0; i < unsignedShortToInt; i++) {
            this.activeTitles[i] = new activeTitle();
            this.activeTitles[i].titleId = readByteArray(4);
            this.activeTitles[i].titleDeposition = readByteArray(2);
            this.activeTitles[i].productId = readByteArray(16);
            this.activeTitles[i].sandboxId = readByteArray(16);
            this.activeTitles[i].aumId = readSgString();
        }
    }

    @Override // packet.MessageResponseProtected
    public void printMessageProtectedData() {
        Log.e("ConsoleStatusProtected", "liveTvProvider: " + Util.byteArrayToHexString(this.liveTvProvider, true));
        Log.e("ConsoleStatusProtected", "majorVersion: " + Util.byteArrayToHexString(this.majorVersion, true));
        Log.e("ConsoleStatusProtected", "minorVersion: " + Util.byteArrayToHexString(this.minorVersion, true));
        Log.e("ConsoleStatusProtected", "buildNumber: " + Util.byteArrayToHexString(this.buildNumber, true));
        Log.e("ConsoleStatusProtected", "local: " + Util.hexByteArrayToASCIIString(this.local));
        Log.e("ConsoleStatusProtected", "activeTitleCount: " + Util.byteArrayToHexString(this.activeTitleCount, true));
        printActiveTitles();
    }

    private void printActiveTitles() {
        for (int i = 0; i < this.activeTitles.length; i++) {
            Log.e("ConsoleStatusProtected", "titleId: " + Util.byteArrayToHexString(this.activeTitles[i].titleId, true));
            Log.e("ConsoleStatusProtected", "titleDeposition: " + Util.byteArrayToHexString(this.activeTitles[i].titleDeposition, true));
            Log.e("ConsoleStatusProtected", "productId: " + Util.byteArrayToHexString(this.activeTitles[i].productId, true));
            Log.e("ConsoleStatusProtected", "sandboxId: " + Util.byteArrayToHexString(this.activeTitles[i].sandboxId, true));
            Log.e("ConsoleStatusProtected", "aumId: " + Util.hexByteArrayToASCIIString(this.activeTitles[i].aumId));
        }
    }
}
