package com.studio08.xbgamestream.Servers;

import android.content.Context;
import com.anggrayudi.storage.file.MimeType;
import com.google.android.exoplayer2.util.MimeTypes;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
/* loaded from: /app/base.apk/classes3.dex */
public class FileServer extends NanoHTTPD {
    Context context;
    String file;
    String fileType;

    public FileServer(String str, String str2, Context context) {
        super(0);
        this.file = str;
        this.fileType = str2;
        this.context = context;
    }

    public void setUrl(String str) {
        this.file = str;
    }

    @Override // fi.iki.elonen.NanoHTTPD
    public NanoHTTPD.Response serve(String str, NanoHTTPD.Method method, Map<String, String> map, Map<String, String> map2, Map<String, String> map3) {
        String str2;
        File file = new File(this.file);
        String str3 = this.fileType;
        if (str3 != null && str3.equals("audio")) {
            str2 = MimeType.AUDIO;
        } else {
            String str4 = this.fileType;
            if (str4 != null && str4.equals("image")) {
                str2 = MimeTypes.IMAGE_JPEG;
            } else {
                str2 = MimeType.VIDEO;
            }
        }
        return serveFile(str, map, file, str2);
    }

    public NanoHTTPD.Response createResponse(NanoHTTPD.Response.Status status, String str, InputStream inputStream) {
        NanoHTTPD.Response newChunkedResponse = newChunkedResponse(status, str, inputStream);
        newChunkedResponse.addHeader("Accept-Ranges", "bytes");
        return newChunkedResponse;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0089  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0136 A[Catch: IOException -> 0x016a, TryCatch #1 {IOException -> 0x016a, blocks: (B:36:0x00da, B:37:0x0125, B:39:0x0136, B:40:0x013e), top: B:50:0x0087 }] */
    /* JADX WARN: Removed duplicated region for block: B:40:0x013e A[Catch: IOException -> 0x016a, TRY_LEAVE, TryCatch #1 {IOException -> 0x016a, blocks: (B:36:0x00da, B:37:0x0125, B:39:0x0136, B:40:0x013e), top: B:50:0x0087 }] */
    /* JADX WARN: Type inference failed for: r16v2 */
    /* JADX WARN: Type inference failed for: r16v22 */
    /* JADX WARN: Type inference failed for: r16v23 */
    /* JADX WARN: Type inference failed for: r16v24 */
    /* JADX WARN: Type inference failed for: r16v3, types: [long] */
    /* JADX WARN: Type inference failed for: r16v4 */
    /* JADX WARN: Type inference failed for: r16v6 */
    /* JADX WARN: Type inference failed for: r24v0, types: [com.studio08.xbgamestream.Servers.FileServer] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private fi.iki.elonen.NanoHTTPD.Response serveFile(java.lang.String r25, java.util.Map<java.lang.String, java.lang.String> r26, java.io.File r27, java.lang.String r28) {
        /*
            Method dump skipped, instructions count: 372
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.Servers.FileServer.serveFile(java.lang.String, java.util.Map, java.io.File, java.lang.String):fi.iki.elonen.NanoHTTPD$Response");
    }
}
