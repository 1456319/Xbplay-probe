package packet;

import android.util.Log;
import org.json.JSONObject;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class JsonMessageResponse extends MessageResponseProtected {
    public byte[] jsonBody;
    public String jsonBodyText;
    public JSONObject jsonDecoded;

    public JsonMessageResponse(byte[] bArr) {
        super(bArr);
        byte[] readSgString = readSgString();
        this.jsonBody = readSgString;
        this.jsonBodyText = Util.hexByteArrayToASCIIString(readSgString);
        try {
            this.jsonDecoded = new JSONObject(this.jsonBodyText);
        } catch (Exception unused) {
            Log.e("Err", "Error decoding json packet");
        }
    }

    public String getJsonStringValue(String str) {
        try {
            return this.jsonDecoded.getString(str);
        } catch (Exception unused) {
            Log.i("ERR", "Error retrieving json value: " + str);
            return null;
        }
    }

    public int getJsonIntValue(String str) {
        try {
            return this.jsonDecoded.getInt(str);
        } catch (Exception unused) {
            Log.i("ERR", "Error retrieving json value: " + str);
            return -1;
        }
    }

    public boolean getJsonBooleanValue(String str) {
        try {
            return this.jsonDecoded.getBoolean(str);
        } catch (Exception unused) {
            Log.i("ERR", "Error retrieving json value: " + str);
            return false;
        }
    }

    @Override // packet.MessageResponseProtected
    public void printMessageProtectedData() {
        Log.e("JsonMessageResponse", "jsonBody: " + Util.hexByteArrayToASCIIString(this.jsonBody));
    }
}
