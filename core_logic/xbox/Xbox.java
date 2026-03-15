package xbox;

import Interfaces.ChannelEvents;
import Interfaces.SmartglassEvents;
import android.util.Log;
import channels.Channel;
import channels.ChannelManager;
import channels.SystemBroadcastChannel;
import channels.SystemInputChannel;
import com.google.android.gms.cast.MediaError;
import constants.ChannelTypes;
import constants.Statuses;
import crypto.SgCrypto;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import nano.base.Nano;
import packet.MessagePacket;
import packet.MessageResponse;
import packet.SimplePacket;
import packet.receive.ConnectionSimpleResponse;
import packet.receive.OpenChannelMessageResponse;
import packet.send.AckMessagePacket;
import packet.send.ConnectSimplePacket;
import packet.send.DiscoverSimplePacket;
import packet.send.LocalJoinMessagePacket;
import packet.send.PowerOffMessagePacket;
import packet.send.WakeSimplePacket;
import util.Util;
/* loaded from: /app/base.apk/classes5.dex */
public class Xbox {
    public String certificate;
    ChannelManager channelManager;
    public String consoleId;

    /* renamed from: crypto  reason: collision with root package name */
    public SgCrypto f32crypto;
    public String ip;
    SmartglassEvents listener;
    public String liveId;
    public byte[] participantId;
    public DatagramSocket socket;
    public String uuid;
    public int requestNumber = 1;
    boolean bKeepRunning = false;
    public boolean connected = false;
    public boolean discovered = false;
    public long lastAckPacket = 0;
    public boolean isSendingSequence = false;

    public void setListener(SmartglassEvents smartglassEvents) {
        this.listener = smartglassEvents;
    }

    public DatagramPacket discover() {
        DiscoverSimplePacket discoverSimplePacket = new DiscoverSimplePacket();
        try {
            DatagramPacket send = discoverSimplePacket.send(discoverSimplePacket.createFullPacket(), InetAddress.getByName(Util.XBOX_DISCOVERY_IP), Util.XBOX_PORT);
            this.ip = send.getAddress().getHostAddress();
            this.discovered = true;
            Log.e("HERE", "Xbox discovered!! Connecting");
            return send;
        } catch (Exception e) {
            Log.e(MediaError.ERROR_TYPE_ERROR, "Error discovering Xbox: " + e.getMessage());
            return null;
        }
    }

    public Nano getNano() {
        Channel channel = getChannel(ChannelTypes.SYSTEM_BROADCAST);
        if (channel != null) {
            SystemBroadcastChannel systemBroadcastChannel = (SystemBroadcastChannel) channel;
            if (systemBroadcastChannel.f8nano != null) {
                return systemBroadcastChannel.f8nano;
            }
            return null;
        }
        return null;
    }

    public DatagramPacket connect() {
        DatagramPacket datagramPacket = null;
        try {
            datagramPacket = send(new ConnectSimplePacket(this.f32crypto));
            ConnectionSimpleResponse connectionSimpleResponse = new ConnectionSimpleResponse(Util.copyHeadBytes(datagramPacket.getData(), datagramPacket.getLength()), this.f32crypto);
            this.participantId = connectionSimpleResponse.participantId;
            this.liveId = connectionSimpleResponse.f31crypto.xboxLiveId;
            this.socket = new DatagramSocket();
            this.channelManager = new ChannelManager(this);
            this.connected = true;
            return datagramPacket;
        } catch (Exception e) {
            Log.e("Here", "Error connecting: " + e.getMessage());
            return datagramPacket;
        }
    }

    public DatagramPacket localJoin() {
        send(new LocalJoinMessagePacket(this.f32crypto));
        return null;
    }

    public void openChannels(ChannelEvents channelEvents) {
        this.channelManager.openChannel(ChannelTypes.SYSTEM_INPUT, channelEvents);
    }

    public Channel getChannel(byte[] bArr) {
        return this.channelManager.getChannelByType(bArr);
    }

    public void sendSystemInputCommand(byte[] bArr) {
        if (!this.connected) {
            Log.e("HRE", "Attempted to send system in put command on disconnected Xbox");
            return;
        }
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        Log.e("HERE", (currentTimeMillis - this.lastAckPacket) + " - seconds since last ack pack");
        if (currentTimeMillis - this.lastAckPacket > 30) {
            disconnect();
        }
        ((SystemInputChannel) this.channelManager.getChannelByType(ChannelTypes.SYSTEM_INPUT)).sendCommand(bArr);
    }

    public void powerOff() {
        PowerOffMessagePacket powerOffMessagePacket = new PowerOffMessagePacket(this.f32crypto);
        powerOffMessagePacket.setLiveId(this.liveId);
        send(powerOffMessagePacket);
    }

    public void powerOn() {
        send(new WakeSimplePacket(this.liveId));
    }

    public void send(MessagePacket messagePacket) {
        try {
            messagePacket.setSequenceNumber(getRequestNumber());
            messagePacket.setParticipantId(this.participantId);
            messagePacket.loadProtectedData();
            byte[] createFullPacket = messagePacket.createFullPacket();
            this.socket.send(new DatagramPacket(createFullPacket, createFullPacket.length, InetAddress.getByName(this.ip), Util.XBOX_PORT));
            Log.i("RawPacketFinal", "" + Util.byteArrayToHexString(createFullPacket, true));
        } catch (Exception unused) {
        }
    }

    public DatagramPacket send(SimplePacket simplePacket) {
        try {
            simplePacket.loadProtectedData();
            return simplePacket.send(simplePacket.createFullPacket(), InetAddress.getByName(this.ip), Util.XBOX_PORT);
        } catch (Exception unused) {
            return null;
        }
    }

    public int getRequestNumber() {
        int i = this.requestNumber + 1;
        this.requestNumber = i;
        return i;
    }

    public void listenForPackets() {
        Log.e("PacketReceiver", "Ready to receive broadcast packets!");
        new Thread() { // from class: xbox.Xbox.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Xbox.this.bKeepRunning = true;
                DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
                while (Xbox.this.bKeepRunning) {
                    try {
                        Xbox.this.socket.receive(datagramPacket);
                        Log.i("ReceivedData:", "Received packet in xbox!: " + Util.byteArrayToHexString(datagramPacket.getData(), true));
                        Xbox.this.lastAckPacket = System.currentTimeMillis() / 1000;
                        MessageResponse messageResponse = new MessageResponse(Util.copyHeadBytes(datagramPacket.getData(), datagramPacket.getLength()), Xbox.this.f32crypto);
                        if (messageResponse.ackFlag) {
                            AckMessagePacket ackMessagePacket = new AckMessagePacket(Xbox.this.f32crypto);
                            ackMessagePacket.setLowWatermark(messageResponse.sequence_number);
                            ackMessagePacket.addProcessedList(messageResponse.sequence_number);
                            Xbox.this.send(ackMessagePacket);
                            Log.i("Received Msg", "REQUIRED TO SEND ACK PACK");
                        } else {
                            Log.i("Received Msg", "NOT SENDING ACK PACK");
                        }
                        if (messageResponse.messageTypeFlag == 39) {
                            Log.i("Received Msg", "Received open channel response");
                            byte[] bArr = ((OpenChannelMessageResponse) messageResponse.protectedDataProcessed).result;
                            if (Arrays.equals(bArr, Statuses.CHANNEL_OPEN_SUCCESS_STATUS)) {
                                if (!Xbox.this.channelManager.addChannel((OpenChannelMessageResponse) messageResponse.protectedDataProcessed)) {
                                    Log.e("ERR", "Failed to open channel");
                                }
                            } else {
                                Log.e("ERR", "NOT ABLE TO OPEN CHANNEL, INVALID RESULT: " + Util.byteArrayToHexString(bArr, false));
                            }
                        } else if (!Arrays.equals(messageResponse.channel_id, ChannelTypes.ACK_CHANNEL)) {
                            Log.i("LIB", "Routing ack to custom channel!" + Util.byteArrayToHexString(messageResponse.channel_id, true));
                            Channel channelByRequestId = Xbox.this.channelManager.getChannelByRequestId(messageResponse.channel_id);
                            if (channelByRequestId != null) {
                                channelByRequestId.receive(messageResponse);
                            } else {
                                Log.i("Err", "Received packet that has a bad channel id: " + Util.byteArrayToHexString(messageResponse.channel_id, true));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Received", "AHHHHHHHHHHHHH ERROR" + e.getMessage());
                        Xbox.this.disconnect();
                    }
                }
            }
        }.start();
        do {
        } while (!this.bKeepRunning);
    }

    public void disconnect() {
        this.bKeepRunning = false;
        this.connected = false;
        Log.e("HERE", "XBOX disconnect called");
        SmartglassEvents smartglassEvents = this.listener;
        if (smartglassEvents != null) {
            smartglassEvents.xboxDisconnected();
        }
    }
}
