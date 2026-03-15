package nano;

import nano.base.Nano;
/* loaded from: /app/base.apk/classes4.dex */
public class GameController {
    int controllerIndex;

    /* renamed from: nano  reason: collision with root package name */
    Nano f17nano;
    public boolean connected = false;
    GameControllerButtonModel buttonModel = new GameControllerButtonModel();

    public GameController(int i) {
        this.controllerIndex = i;
    }

    public void pressInputButton(int i) {
        this.buttonModel.pressInputButton(i);
        this.f17nano.pressControllerButton(this.buttonModel);
    }

    public void pressAnalogButton(int i, byte[] bArr, boolean z) {
        this.buttonModel.pressAnalogButton(i, bArr);
        if (z) {
            this.f17nano.pressControllerButton(this.buttonModel);
        }
    }

    public void setNano(Nano nano2) {
        this.f17nano = nano2;
    }

    public void connect() {
        if (this.connected) {
            return;
        }
        this.f17nano.connectController(this.controllerIndex);
        this.connected = true;
    }
}
