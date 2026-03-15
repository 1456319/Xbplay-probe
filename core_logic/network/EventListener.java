package network;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
/* loaded from: /app/base.apk/classes4.dex */
public class EventListener extends ResultReceiver {
    private Receiver receiver;

    /* loaded from: /app/base.apk/classes4.dex */
    public interface Receiver {
        void onReceiveResult(int i, Bundle bundle);
    }

    public EventListener(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override // android.os.ResultReceiver
    protected void onReceiveResult(int i, Bundle bundle) {
        Receiver receiver = this.receiver;
        if (receiver != null) {
            receiver.onReceiveResult(i, bundle);
        }
    }
}
