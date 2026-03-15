package com.studio08.xbgamestream.Timers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.RewardedAdLoader;
import com.studio08.xbgamestream.Web.ApiClient;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class PurchaseChecker {
    Context context;
    PCheckInterface listener;
    Runnable runnable;
    Handler handler = new Handler();
    int delay = 300000;

    public PurchaseChecker(Context context, PCheckInterface pCheckInterface) {
        this.context = context;
        this.listener = pCheckInterface;
    }

    public void start() {
        try {
            Handler handler = this.handler;
            if (handler != null) {
                Runnable runnable = new Runnable() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Log.e("PCheck", "PCheck started");
                        PurchaseChecker.this.handler.postDelayed(PurchaseChecker.this.runnable, PurchaseChecker.this.delay);
                        if (PurchaseChecker.this.listener != null) {
                            PurchaseChecker.this.listener.PCheckTriggered();
                        }
                    }
                };
                this.runnable = runnable;
                handler.postDelayed(runnable, this.delay);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            Handler handler = this.handler;
            if (handler != null) {
                handler.removeCallbacks(this.runnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLicenseCheckDialog(final boolean z) {
        AlertDialog.Builder positiveButton = new AlertDialog.Builder(this.context).setTitle("Unlock Full Version").setMessage("1. Download the official XBPlay app from Google Play or the Apple App Store on any mobile device.\n2. Login to your Microsoft account in the XBPlay mobile app.\n3. Purchase the full version of the app by clicking the 'Unlock Full Version' button in the settings of the mobile app.\n4. Return to this app and click the check license button below.\n\nIf you followed step 1 through 3 correctly, it will unlock this app.").setCancelable(z).setPositiveButton("Check License", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor edit = PurchaseChecker.this.context.getSharedPreferences("SettingsSharedPref", 0).edit();
                edit.putLong("nextMakeGetTokenRequest", 0L);
                edit.apply();
                PurchaseChecker.this.doLookupPCheck(z, false, false);
            }
        });
        if (z) {
            positiveButton.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
        } else {
            positiveButton.setNegativeButton("Exit App", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.4
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((Activity) PurchaseChecker.this.context).finishAffinity();
                }
            });
        }
        positiveButton.show();
    }

    public void doLookupPCheck(final boolean z, final boolean z2, boolean z3) {
        try {
            final String value = new EncryptClient(this.context).getValue("gsToken");
            if (TextUtils.isEmpty(value)) {
                if (z2) {
                    return;
                }
                createPopup("Sign-in Required", "You must sign-in to your Xbox Live account first. Click the Login button, then try again.");
            } else if (!RewardedAdLoader.shouldCheckNewToken(this.context) && z3) {
                Log.e("Purchase", "Not checking token. Not expired yet");
            } else {
                Volley.newRequestQueue(this.context).add(new StringRequest(1, ApiClient.TOKEN_GET_BASE_URL, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.5
                    @Override // com.android.volley.Response.Listener
                    public void onResponse(String str) {
                        try {
                            int i = new JSONObject(str).getInt("activePurchase");
                            boolean z4 = true;
                            if (i == 1 && !z2) {
                                PurchaseChecker.this.createPopup("License Granted!", "You have successfully unlocked the full version of this app. Thank you!");
                            }
                            if (i != 1) {
                                z4 = false;
                            }
                            RewardedAdLoader.setPurchaseItem(z4, PurchaseChecker.this.context);
                            SharedPreferences.Editor edit = PurchaseChecker.this.context.getSharedPreferences("SettingsSharedPref", 0).edit();
                            edit.putLong("nextMakeGetTokenRequest", System.currentTimeMillis() + RewardedAdLoader.GET_TOKEN_CACHE_DURATION);
                            edit.apply();
                            if (i == 0) {
                                if (!z2) {
                                    PurchaseChecker.this.createFailureDialog(z);
                                } else {
                                    Toast.makeText(PurchaseChecker.this.context, "License Check Failed", 0).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            RewardedAdLoader.setPurchaseItem(false, PurchaseChecker.this.context);
                            if (!z2) {
                                PurchaseChecker.this.createFailureDialog(z);
                            } else {
                                Toast.makeText(PurchaseChecker.this.context, "License Check Failed", 0).show();
                            }
                        }
                    }
                }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.6
                    @Override // com.android.volley.Response.ErrorListener
                    public void onErrorResponse(VolleyError volleyError) {
                        RewardedAdLoader.setPurchaseItem(false, PurchaseChecker.this.context);
                        if (!z2) {
                            PurchaseChecker.this.createFailureDialog(z);
                        } else {
                            Toast.makeText(PurchaseChecker.this.context, "License Check Failed", 0).show();
                        }
                    }
                }) { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.7
                    @Override // com.android.volley.Request
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override // com.android.volley.Request
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("gsToken", value);
                            return jSONObject.toString().getBytes(StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void createPopup(String str, String str2) {
        try {
            new AlertDialog.Builder(this.context).setTitle(str).setMessage(str2).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.8
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void createFailureDialog(final boolean z) {
        try {
            AlertDialog.Builder positiveButton = new AlertDialog.Builder(this.context).setTitle("License Check Failed").setMessage("License not found. Ensure you signed in and purchased the XBPlay app. Then try again.").setCancelable(z).setNegativeButton("Exit App", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.10
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((Activity) PurchaseChecker.this.context).finishAffinity();
                }
            }).setPositiveButton("Try Again", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.9
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    PurchaseChecker.this.doLookupPCheck(z, false, false);
                }
            });
            if (!z) {
                positiveButton.setNegativeButton("Exit App", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.12
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((Activity) PurchaseChecker.this.context).finishAffinity();
                    }
                });
            } else {
                positiveButton.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Timers.PurchaseChecker.11
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
            }
            positiveButton.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
