package com.studio08.xbgamestream.Servers;

import android.content.Context;
import android.content.SharedPreferences;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Web.ApiClient;
import fi.iki.elonen.NanoHTTPD;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/* loaded from: /app/base.apk/classes3.dex */
public class Server extends NanoHTTPD {
    public static int PORT = 3000;
    Context context;
    String gsTokenPwa;
    String serverIdPwa;

    public Server(int i, Context context) {
        super(i);
        this.gsTokenPwa = "";
        this.serverIdPwa = "";
        this.context = context;
    }

    public Server(int i, Context context, String str, String str2) {
        super(i);
        this.context = context;
        this.gsTokenPwa = str;
        this.serverIdPwa = str2;
    }

    @Override // fi.iki.elonen.NanoHTTPD
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession iHTTPSession) {
        try {
            return newFixedLengthResponse(readFile("play-anywhere.html"));
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse("<html><body><h1>Error Creating Local Server :(</h1></body></html>");
        }
    }

    private String readFile(String str) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.context.getAssets().open(str), "UTF-8"));
        try {
            StringBuilder sb = new StringBuilder();
            for (String readLine = bufferedReader.readLine(); readLine != null; readLine = bufferedReader.readLine()) {
                sb.append(injectUrls(readLine));
                sb.append("\n");
            }
            return sb.toString();
        } finally {
            bufferedReader.close();
        }
    }

    private String injectUrls(String str) {
        return str.contains("STREAM_VIEW_URL") ? str.replace("STREAM_VIEW_URL", ApiClient.STREAMING_URL + queryStringParams()) : str;
    }

    private String queryStringParams() {
        EncryptClient encryptClient = new EncryptClient(this.context);
        String value = encryptClient.getValue("serverId");
        String value2 = encryptClient.getValue("gsToken");
        if (!this.gsTokenPwa.equals("")) {
            value2 = this.gsTokenPwa;
        }
        if (!this.serverIdPwa.equals("")) {
            value = this.serverIdPwa;
        }
        if (value.equals("")) {
            value = "-1";
        }
        if (value2.equals("")) {
            value2 = "-1";
        }
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("SettingsSharedPref", 0);
        String string = sharedPreferences.getString("video_fit_key", "cover");
        Integer valueOf = Integer.valueOf(sharedPreferences.getInt("video_vertical_offset_key", 50));
        String string2 = sharedPreferences.getString("emulate_client_key", "windows");
        String string3 = sharedPreferences.getString("controller_refresh_key", "32");
        String str = "?gsToken=" + value2 + "&serverId=" + value + "&originId=" + Helper.getLocalIpAddress() + ":" + PORT + "&video-fit=" + string + "&video-vertical-offset=" + valueOf + "&userAgentType=" + string2 + "&gamepadRefreshRateMs=" + string3 + "&miniGamepadSize=" + sharedPreferences.getString("mini_gamepad_size_key", "30") + "&maxBitrate=" + sharedPreferences.getString("max_bitrate_key", "");
        return !Boolean.valueOf(sharedPreferences.getBoolean("enable_audio_default_key", true)).booleanValue() ? str + "&disable-audio=true" : str;
    }
}
