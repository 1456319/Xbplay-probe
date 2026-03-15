package com.studio08.xbgamestream.Servers;

import android.util.Log;
import com.anggrayudi.storage.file.MimeType;
import com.connectsdk.discovery.DiscoveryProvider;
import com.google.android.exoplayer2.util.MimeTypes;
import com.studio08.xbgamestream.Helpers.Helper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.http.message.TokenParser;
import org.mozilla.thirdparty.com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
/* loaded from: /app/base.apk/classes3.dex */
public class Xbox360FileServer implements Runnable {
    private static final String TAG = "com.studio08.xbgamestream.Servers.Xbox360FileServer";
    private long cbSkip;
    private String fileType;
    private File mMovieFile;
    private boolean seekRequest;
    private ServerSocket socket;
    private Thread thread;
    private int port = 0;
    private boolean isRunning = false;

    public Xbox360FileServer(File file) {
        this.mMovieFile = file;
    }

    public int getPort() {
        return this.port;
    }

    public String init(String str) {
        this.fileType = str;
        String str2 = null;
        try {
            ServerSocket serverSocket = new ServerSocket(this.port, 0, InetAddress.getByAddress(InetAddress.getByName(Helper.getLocalIpAddress()).getAddress()));
            this.socket = serverSocket;
            serverSocket.setSoTimeout(DiscoveryProvider.TIMEOUT);
            this.port = this.socket.getLocalPort();
            str2 = "http://" + this.socket.getInetAddress().getHostAddress() + ":" + this.port;
            Log.e(TAG, "Server started at " + str2);
            return str2;
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error UnknownHostException server", e);
            return str2;
        } catch (IOException e2) {
            Log.e(TAG, "Error IOException server", e2);
            return str2;
        }
    }

    public String getFileUrl() {
        String str;
        if (!this.fileType.contains("audio")) {
            str = ".mp4";
        } else {
            str = DefaultHlsExtractorFactory.MP3_FILE_EXTENSION;
        }
        return "http://" + this.socket.getInetAddress().getHostAddress() + ":" + this.port + "/video" + str;
    }

    public void start() {
        Thread thread = new Thread(this);
        this.thread = thread;
        thread.start();
        this.isRunning = true;
    }

    public void stop() {
        this.isRunning = false;
        if (this.thread == null) {
            Log.e(TAG, "Server was stopped without being started.");
            return;
        }
        Log.e(TAG, "Stopping server.");
        this.socket = null;
        this.thread.interrupt();
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Override // java.lang.Runnable
    public void run() {
        Log.e(TAG, "running");
        while (this.isRunning) {
            try {
                Socket accept = this.socket.accept();
                if (accept != null) {
                    String str = TAG;
                    Log.e(str, "client connected at " + this.port);
                    ExternalResourceDataSource externalResourceDataSource = new ExternalResourceDataSource(this.mMovieFile);
                    Log.e(str, "processing request...");
                    processRequest(externalResourceDataSource, accept);
                }
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "No client connected, waiting for client...", e);
            } catch (IOException unused) {
            }
        }
        Log.e(TAG, "Server interrupted or stopped. Shutting down.");
    }

    private int findHeaderEnd(byte[] bArr, int i) {
        int i2 = 0;
        while (true) {
            int i3 = i2 + 3;
            if (i3 >= i) {
                return 0;
            }
            if (bArr[i2] == 13 && bArr[i2 + 1] == 10 && bArr[i2 + 2] == 13 && bArr[i3] == 10) {
                return i2 + 4;
            }
            i2++;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:45:0x027a, code lost:
        if (r1 != null) goto L51;
     */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x0292, code lost:
        if (0 == 0) goto L49;
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x02b8, code lost:
        if (0 == 0) goto L49;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void processRequest(com.studio08.xbgamestream.Servers.Xbox360FileServer.ExternalResourceDataSource r11, java.net.Socket r12) throws java.lang.IllegalStateException, java.io.IOException {
        /*
            Method dump skipped, instructions count: 709
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.Servers.Xbox360FileServer.processRequest(com.studio08.xbgamestream.Servers.Xbox360FileServer$ExternalResourceDataSource, java.net.Socket):void");
    }

    private void decodeHeader(BufferedReader bufferedReader, Properties properties, Properties properties2, Properties properties3) throws InterruptedException {
        String decodePercent;
        try {
            String readLine = bufferedReader.readLine();
            if (readLine == null) {
                return;
            }
            StringTokenizer stringTokenizer = new StringTokenizer(readLine);
            if (!stringTokenizer.hasMoreTokens()) {
                Log.e(TAG, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
            }
            properties.put("method", stringTokenizer.nextToken());
            if (!stringTokenizer.hasMoreTokens()) {
                Log.e(TAG, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
            }
            String nextToken = stringTokenizer.nextToken();
            int indexOf = nextToken.indexOf(63);
            if (indexOf >= 0) {
                decodeParms(nextToken.substring(indexOf + 1), properties2);
                decodePercent = decodePercent(nextToken.substring(0, indexOf));
            } else {
                decodePercent = decodePercent(nextToken);
            }
            if (stringTokenizer.hasMoreTokens()) {
                String readLine2 = bufferedReader.readLine();
                while (readLine2 != null && readLine2.trim().length() > 0) {
                    int indexOf2 = readLine2.indexOf(58);
                    if (indexOf2 >= 0) {
                        properties3.put(readLine2.substring(0, indexOf2).trim().toLowerCase(), readLine2.substring(indexOf2 + 1).trim());
                    }
                    readLine2 = bufferedReader.readLine();
                }
            }
            properties.put("uri", decodePercent);
        } catch (IOException e) {
            Log.e(TAG, "SERVER INTERNAL ERROR: IOException: " + e.getMessage());
        }
    }

    private void decodeParms(String str, Properties properties) throws InterruptedException {
        if (str == null) {
            return;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(str, "&");
        while (stringTokenizer.hasMoreTokens()) {
            String nextToken = stringTokenizer.nextToken();
            int indexOf = nextToken.indexOf(61);
            if (indexOf >= 0) {
                properties.put(decodePercent(nextToken.substring(0, indexOf)).trim(), decodePercent(nextToken.substring(indexOf + 1)));
            }
        }
    }

    private String decodePercent(String str) throws InterruptedException {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            int i = 0;
            while (i < str.length()) {
                char charAt = str.charAt(i);
                if (charAt == '%') {
                    stringBuffer.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
                    i += 2;
                } else if (charAt == '+') {
                    stringBuffer.append(TokenParser.SP);
                } else {
                    stringBuffer.append(charAt);
                }
                i++;
            }
            return stringBuffer.toString();
        } catch (Exception unused) {
            Log.e(TAG, "BAD REQUEST: Bad percent-encoding.");
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: /app/base.apk/classes3.dex */
    public class ExternalResourceDataSource {
        long contentLength;
        private FileInputStream inputStream;
        private final File movieResource;

        public ExternalResourceDataSource(File file) {
            this.movieResource = file;
            this.contentLength = file.length();
            Log.e(Xbox360FileServer.TAG, "respurcePath is: " + Xbox360FileServer.this.mMovieFile.getPath());
        }

        public String getContentType() {
            if (!Xbox360FileServer.this.fileType.contains("audio")) {
                if (Xbox360FileServer.this.fileType.contains("image")) {
                    return MimeTypes.IMAGE_JPEG;
                }
                return MimeType.VIDEO;
            }
            return MimeType.AUDIO;
        }

        public InputStream createInputStream() throws IOException {
            getInputStream();
            return this.inputStream;
        }

        public long getContentLength(boolean z) {
            return this.contentLength;
        }

        private void getInputStream() {
            try {
                this.inputStream = new FileInputStream(this.movieResource);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            this.contentLength = this.movieResource.length();
            Log.e(Xbox360FileServer.TAG, "file exists??" + this.movieResource.exists() + " and content length is: " + this.contentLength);
        }
    }
}
