package com.studio08.xbgamestream.Servers;

import android.text.TextUtils;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.Map;
/* loaded from: /app/base.apk/classes3.dex */
public class TvServer extends NanoHTTPD {
    public static final int PORT = 9087;
    private ConnectionListener listener;

    /* loaded from: /app/base.apk/classes3.dex */
    public interface ConnectionListener {
        void onValidConnection(String str, String str2, String str3, String str4);
    }

    public TvServer(ConnectionListener connectionListener) throws IOException {
        super(PORT);
        this.listener = connectionListener;
    }

    @Override // fi.iki.elonen.NanoHTTPD
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession iHTTPSession) {
        if (iHTTPSession.getUri().equals("/startSession")) {
            Map<String, String> parms = iHTTPSession.getParms();
            String str = parms.get("gsToken");
            String str2 = parms.get("serverId");
            String str3 = parms.get("xcloudToken");
            String str4 = parms.get("msalToken");
            if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
                return newFixedLengthResponse("Error 1038931");
            }
            this.listener.onValidConnection(str, str2, str3, str4);
            return newFixedLengthResponse("ok");
        }
        return null;
    }
}
