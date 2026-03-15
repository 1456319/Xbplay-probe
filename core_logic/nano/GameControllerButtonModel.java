package nano;

import constants.GameControllerAnalogMappings;
import java.util.Arrays;
/* loaded from: /app/base.apk/classes4.dex */
public class GameControllerButtonModel {
    private byte[] buttonModel;
    private byte[] analogModel = new byte[14];
    private byte[] extensionModel = new byte[9];

    /* JADX INFO: Access modifiers changed from: package-private */
    public GameControllerButtonModel() {
        byte[] bArr = new byte[16];
        this.buttonModel = bArr;
        Arrays.fill(bArr, (byte) 0);
        Arrays.fill(this.analogModel, (byte) 0);
        Arrays.fill(this.extensionModel, (byte) 0);
    }

    public void pressInputButton(int i) {
        byte[] bArr = this.buttonModel;
        bArr[i] = (byte) (bArr[i] + 1);
    }

    public void pressAnalogButton(int i, byte[] bArr) {
        if (i == GameControllerAnalogMappings.leftThubstickX || i == GameControllerAnalogMappings.leftThubstickY || i == GameControllerAnalogMappings.rightThumbstickX || i == GameControllerAnalogMappings.rightThumbstickY) {
            byte[] bArr2 = this.analogModel;
            bArr2[i] = bArr[0];
            bArr2[i + 1] = bArr[1];
            return;
        }
        byte[] bArr3 = this.analogModel;
        bArr3[i] = (byte) (bArr3[i] + 1);
    }

    public byte[] getButtonModel() {
        return this.buttonModel;
    }

    public byte[] getAnalogModel() {
        return this.analogModel;
    }

    public byte[] getExtensionModel() {
        return this.extensionModel;
    }
}
