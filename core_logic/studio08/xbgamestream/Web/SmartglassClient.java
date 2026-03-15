package com.studio08.xbgamestream.Web;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.studio08.xbgamestream.Authenticate.LoginClientV4;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.tapjoy.TJAdUnitConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class SmartglassClient {
    Context mContext;

    public SmartglassClient(Context context) {
        this.mContext = context;
    }

    public void sendSmartglassCommand(String str) {
        try {
            Log.d("PWAMM", "sendSmartglassCommand: " + str);
            Helper.vibrate(this.mContext);
            JSONObject jSONObject = new EncryptClient(this.mContext).getJSONObject("xalData");
            if (jSONObject == null) {
                Toast.makeText(this.mContext, "Login Required", 0).show();
                return;
            }
            Pair<String, String> extractWebTokenAndUhs = extractWebTokenAndUhs(jSONObject);
            String str2 = (String) extractWebTokenAndUhs.second;
            JSONObject createSmartglassPayload = createSmartglassPayload(str);
            Log.d("PWAMM", "Payload: " + createSmartglassPayload.toString());
            hitSmartglassApi(str2, (String) extractWebTokenAndUhs.first, createSmartglassPayload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitSmartglassApi(final String str, final String str2, JSONObject jSONObject) {
        RequestQueue newRequestQueue = Volley.newRequestQueue(this.mContext);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(1, ApiClient.SMARTGLASS_COMMAND_URL, jSONObject, new Response.Listener() { // from class: com.studio08.xbgamestream.Web.SmartglassClient$$ExternalSyntheticLambda0
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                JSONObject jSONObject2 = (JSONObject) obj;
                Log.d("SmartglassCommand", "Response: " + jSONObject2.toString());
            }
        }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Web.SmartglassClient$$ExternalSyntheticLambda1
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                SmartglassClient.this.m347xd7755c6b(volleyError);
            }
        }) { // from class: com.studio08.xbgamestream.Web.SmartglassClient.1
            @Override // com.android.volley.Request
            public Map<String, String> getHeaders() {
                HashMap hashMap = new HashMap();
                hashMap.put("Content-Type", "application/json");
                hashMap.put("Authorization", "XBL3.0 x=" + str + ";" + str2);
                hashMap.put("Accept-Language", "en-US");
                hashMap.put("x-xbl-contract-version", "4");
                hashMap.put("x-xbl-client-name", "XboxApp");
                hashMap.put("skillplatform", "RemoteManagement");
                hashMap.put("x-xbl-client-type", "UWA");
                hashMap.put("x-xbl-client-version", "39.39.22001.0");
                hashMap.put("MS-CV", SessionDescription.SUPPORTED_SDP_VERSION);
                return hashMap;
            }
        };
        jsonObjectRequest.setRetryPolicy(LoginClientV4.volleyPolicy);
        newRequestQueue.add(jsonObjectRequest);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$hitSmartglassApi$1$com-studio08-xbgamestream-Web-SmartglassClient  reason: not valid java name */
    public /* synthetic */ void m347xd7755c6b(VolleyError volleyError) {
        int i = volleyError.networkResponse != null ? volleyError.networkResponse.statusCode : -1;
        if (i == 404) {
            Toast.makeText(this.mContext, "Send failed. Xbox not responding", 0).show();
        } else if (i == 401 || i == 403) {
            Toast.makeText(this.mContext, "Send failed. Try logging in again", 0).show();
        } else if (i != 200) {
            Toast.makeText(this.mContext, "Send failed: " + i, 0).show();
        }
    }

    private JSONObject createSmartglassPayload(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            String string = jSONObject.getString("consoleId");
            String string2 = jSONObject.getString("commandType");
            String string3 = jSONObject.getString("command");
            JSONArray jSONArray = jSONObject.getJSONArray(TJAdUnitConstants.String.BEACON_PARAMS);
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put(FirebaseAnalytics.Param.DESTINATION, "Xbox");
            jSONObject2.put("sessionId", UUID.randomUUID().toString());
            jSONObject2.put("sourceId", "com.microsoft.smartglass");
            jSONObject2.put("type", string2);
            jSONObject2.put("command", string3);
            jSONObject2.put("parameters", jSONArray);
            jSONObject2.put("linkedXboxId", string);
            return jSONObject2;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Pair<String, String> extractWebTokenAndUhs(JSONObject jSONObject) throws JSONException {
        JSONObject jSONObject2 = jSONObject.getJSONObject("webToken").getJSONObject("data");
        return new Pair<>(jSONObject2.getString("Token"), jSONObject2.getJSONObject("DisplayClaims").getJSONArray("xui").getJSONObject(0).getString("uhs"));
    }
}
