package nano.base;

import Interfaces.NanoStreamEvents;
import android.util.Log;
import com.google.android.gms.cast.MediaError;
import com.ironsource.mediationsdk.server.HttpFunctions;
import constants.PacketProtocol;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import nano.GameControllerButtonModel;
import nano.packets.response.ChannelControlResponse;
import nano.packets.response.NanoResponse;
import nano.packets.send.NanoPacket;
import nano.streams.ControlStream;
import nano.streams.ControllerInputStream;
import nano.streams.CoreStream;
import nano.streams.StreamListener;
import nano.streams.VideoStream;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class Nano {
    private boolean bKeepRunning = false;
    private byte[] connectionId;
    private StreamListener controlListener;
    ControlStream controlStream;
    private StreamListener coreListener;
    CoreStream coreStream;
    DataOutputStream dos;
    ControllerInputStream inputStream;
    private StreamListener inputStreamListener;
    private NanoStreamEvents nanoStreamEvents;
    private String sessionId;
    private int tcpPort;
    public Socket tcpSocket;
    private int udpPort;
    public DatagramSocket udpSocket;
    private StreamListener videoListener;
    VideoStream videoStream;
    private String xboxAddress;

    public void init(String str, int i, int i2, String str2) {
        this.udpPort = i;
        this.tcpPort = i2;
        this.sessionId = str2;
        this.xboxAddress = str;
    }

    public void start() {
        initChannels();
        openUdpSocket();
        openTcpSocket();
        listenForUDPPackets();
        listenForTCPPackets();
        this.coreStream.sendControlHandshake();
    }

    public void setNanoStreamListener(NanoStreamEvents nanoStreamEvents) {
        this.nanoStreamEvents = nanoStreamEvents;
    }

    public void setVideoListener(StreamListener streamListener) {
        this.videoListener = streamListener;
    }

    public void setCoreListener(StreamListener streamListener) {
        this.coreListener = streamListener;
    }

    private void initChannels() {
        CoreStream coreStream = new CoreStream(this, this.nanoStreamEvents);
        this.coreStream = coreStream;
        coreStream.setChannelId(new byte[]{0, 0});
        setCoreListener(this.coreStream);
        this.coreStream.setActive();
        VideoStream videoStream = new VideoStream(this, this.nanoStreamEvents);
        this.videoStream = videoStream;
        setVideoListener(videoStream);
        ControlStream controlStream = new ControlStream(this, this.nanoStreamEvents);
        this.controlStream = controlStream;
        this.controlListener = controlStream;
        ControllerInputStream controllerInputStream = new ControllerInputStream(this, this.nanoStreamEvents);
        this.inputStream = controllerInputStream;
        this.inputStreamListener = controllerInputStream;
    }

    private boolean openUdpSocket() {
        try {
            this.udpSocket = new DatagramSocket();
            return true;
        } catch (Exception e) {
            Log.e("ERR", "Cannot open UDP socket on port: " + this.udpPort);
            Log.e("ERR", "Cannot open UDP socket on host: " + this.xboxAddress);
            Log.e("ERR", "UDP ERROR: " + e.getMessage());
            return false;
        }
    }

    private boolean openTcpSocket() {
        try {
            Socket socket = new Socket(InetAddress.getByName(this.xboxAddress), this.tcpPort);
            this.tcpSocket = socket;
            this.dos = new DataOutputStream(socket.getOutputStream());
            return false;
        } catch (Exception unused) {
            Log.e("ERR", "Cannot open TCP socket on port: " + this.tcpSocket);
            Log.e("ERR", "Cannot open TCO socket on host: " + this.xboxAddress);
            return false;
        }
    }

    public void listenForUDPPackets() {
        new Thread() { // from class: nano.base.Nano.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Nano.this.bKeepRunning = true;
                DatagramPacket datagramPacket = new DatagramPacket(new byte[4096], 4096);
                while (Nano.this.bKeepRunning) {
                    try {
                        Nano.this.udpSocket.receive(datagramPacket);
                        Nano.this.coreStream.connected = true;
                        Nano.this.processPacket(Util.copyHeadBytes(datagramPacket.getData(), datagramPacket.getLength()), PacketProtocol.UDP);
                        Log.e("====NANO====:", "Received UDP packet in nano!: " + Util.byteArrayToHexString(datagramPacket.getData(), true));
                    } catch (Exception e) {
                        Log.e("ERR", "Receive UDP packet ERROR: " + e.getMessage());
                        Nano.this.bKeepRunning = false;
                    }
                }
            }
        }.start();
        Log.e("PacketReceiver", "Ready to receive Nano UDP packets!");
    }

    public void listenForTCPPackets() {
        new Thread() { // from class: nano.base.Nano.2
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                DataInputStream dataInputStream;
                Nano.this.bKeepRunning = true;
                try {
                    dataInputStream = new DataInputStream(Nano.this.tcpSocket.getInputStream());
                } catch (Exception unused) {
                    Log.e("ERR", "Error opening tcp input stream");
                    dataInputStream = null;
                }
                while (Nano.this.bKeepRunning) {
                    try {
                        byte[] tcpData = Nano.this.getTcpData(dataInputStream);
                        if (tcpData != null) {
                            Log.i("NANO:", "Received TCP packet in nano!: " + Util.byteArrayToHexString(tcpData, true));
                            Nano.this.processPacket(tcpData, PacketProtocol.TCP);
                        } else {
                            Log.i(HttpFunctions.ERROR_PREFIX, " Error getting tcp packet!");
                            Nano.this.bKeepRunning = false;
                        }
                    } catch (Exception e) {
                        Log.e("ERR", "Receive TCP packet ERROR: " + e.getMessage());
                        Nano.this.bKeepRunning = false;
                    }
                }
            }
        }.start();
        Log.e("PacketReceiver", "Ready to receive Nano TCP packets!");
    }

    public void send(NanoPacket nanoPacket) {
        try {
            byte[] createFullPacket = nanoPacket.createFullPacket();
            nanoPacket.printData("Nano Sending Packet: " + nanoPacket.protocolType);
            InetAddress byName = InetAddress.getByName(this.xboxAddress);
            if (nanoPacket.protocolType == PacketProtocol.UDP) {
                this.udpSocket.send(new DatagramPacket(createFullPacket, createFullPacket.length, byName, this.udpPort));
                Log.w(PacketProtocol.UDP, "Sent Nano UDP Packet");
            } else if (nanoPacket.protocolType != PacketProtocol.TCP) {
                Log.e("ERR", "Invalid packet protocol");
            } else {
                this.dos.write(createFullPacket, 0, createFullPacket.length);
                Log.w(PacketProtocol.TCP, "Sent Nano TCP Packet");
            }
        } catch (Exception e) {
            Log.e("ERR", "Error Sending Nano Packet");
            Log.e("ERR", "" + e.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public byte[] getTcpData(DataInputStream dataInputStream) {
        byte[] bArr = new byte[4];
        try {
            dataInputStream.read(bArr, 0, 4);
            int byteArrayToIntLE = Util.byteArrayToIntLE(bArr);
            if (byteArrayToIntLE > 0) {
                Log.i("NANO:", "Received TCP packet of length!: " + byteArrayToIntLE);
                byte[] bArr2 = new byte[byteArrayToIntLE];
                dataInputStream.readFully(bArr2);
                return bArr2;
            }
            Log.i("NANO:", "no tcp data received");
            return null;
        } catch (Exception e) {
            Log.i("NANO:", "Error trying to read TCP packet" + e.getMessage());
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processPacket(byte[] bArr, String str) {
        NanoResponse nanoResponse = new NanoResponse(bArr);
        if (Arrays.equals(nanoResponse.getChannelId(), new byte[]{0, 0})) {
            this.coreStream.receive(nanoResponse);
        } else if (nanoResponse.getPacketType() == 97) {
            ChannelControlResponse channelControlResponse = new ChannelControlResponse(bArr);
            channelControlResponse.printData();
            handleChannelControlPackets(channelControlResponse);
        } else {
            Log.e("PROCESS_PACKET", "Got a packet that needs routing: " + Util.byteArrayToHexString(nanoResponse.getChannelId(), true));
            if (Arrays.equals(nanoResponse.getChannelId(), this.videoStream.getChannelId())) {
                this.videoStream.receive(nanoResponse);
            } else if (Arrays.equals(nanoResponse.getChannelId(), this.controlStream.getChannelId())) {
                this.controlStream.receive(nanoResponse);
            } else if (Arrays.equals(nanoResponse.getChannelId(), this.inputStream.getChannelId())) {
                this.inputStream.receive(nanoResponse);
            } else {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Unmapped Channel ID Sent: " + Util.byteArrayToHexString(nanoResponse.getChannelId(), true));
            }
        }
    }

    private void handleChannelControlPackets(ChannelControlResponse channelControlResponse) {
        this.controlListener.receiveControlPacket(channelControlResponse);
        this.inputStreamListener.receiveControlPacket(channelControlResponse);
    }

    public void connectController(int i) {
        this.controlStream.sendControllerInit(i);
    }

    public void pressControllerButton(GameControllerButtonModel gameControllerButtonModel) {
        this.inputStream.sendButtonPressed(gameControllerButtonModel);
    }
}
