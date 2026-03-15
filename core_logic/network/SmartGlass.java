package network;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import java.net.DatagramSocket;
import java.util.Random;
import network.BindService;
/* loaded from: /app/base.apk/classes4.dex */
public class SmartGlass {
    public String _client_id;
    public String connection_status;
    public boolean console;
    private String console_ip;
    public Context context;
    public String current_app;
    public EventListener eventListener;
    public boolean interval_timeout;
    public boolean last_received_time;
    private Listeners listener;
    BindService mBoundService;
    public String managers_num;
    public DatagramSocket socket;
    private int port = 5050;
    boolean mServiceBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() { // from class: network.SmartGlass.1
        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            SmartGlass.this.mServiceBound = false;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SmartGlass.this.mBoundService = ((BindService.MyBinder) iBinder).getService();
            SmartGlass.this.mServiceBound = true;
        }
    };
    public int id = new Random().nextInt(999) + 1;
    public String ip = "255.255.255.255";
    public boolean is_broadcast = true;

    public SmartGlass(Context context) {
        this.context = context;
    }

    public void setListener(Listeners listeners) {
        this.listener = listeners;
    }

    public void listenForXbox(Context context) {
        context.getApplicationContext().bindService(new Intent(context.getApplicationContext(), BindService.class), this.mServiceConnection, 1);
    }

    public void test() {
        try {
            if (this.mServiceBound) {
                this.mBoundService.discover();
            }
        } catch (Exception e) {
            Log.e("CALLED", e.getMessage());
        }
    }
}
