package cn.oyzh.easyshell.test.rdp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 最终修复版 RDP 连接
 * 关键修复：在 MCS Connect Response 后，必须先读取服务器初始化数据
 */
public class RDPConnection {
    
    private static final int DEFAULT_RDP_PORT = 3389;
    private static final int READ_TIMEOUT = 15000;
    private static final boolean DEBUG = true;
    
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    
    private String host;
    private int port;
    private String username;
    private String password;
    
    private volatile boolean connected = false;
    
    private int screenWidth = 1024;
    private int screenHeight = 768;
    private int userId = 0;
    
    public RDPConnection(String host, int port, String username, String password) {
        this.host = host;
        this.port = port > 0 ? port : DEFAULT_RDP_PORT;
        this.username = username != null ? username : "user";
        this.password = password != null ? password : "";
    }
    
    public void connect() throws Exception {
        try {
            connectSocket();
            
            step1_X224ConnectionRequest();
            step2_X224ConnectionConfirm();
            step3_MCSConnectInitial();
            step4_MCSConnectResponse();
            
            // 关键修复：在发送 Attach User 之前，必须先读取服务器的初始化数据
            step4_5_ReadServerInitialData();
            
            step5_AttachUserRequest();
            step6_AttachUserResponse();
            step7_ChannelJoins();
            
            connected = true;
            System.out.println("[RDP] ✓✓✓ RDP Connected Successfully! ✓✓✓");
            System.out.println("[RDP] Username: " + username);
            System.out.println("[RDP] User ID: " + userId);
            
        } catch (Exception e) {
            System.err.println("[RDP] ✗ Connection failed: " + e.getMessage());
            e.printStackTrace();
            disconnect();
            throw e;
        }
    }
    
    private void connectSocket() throws IOException {
        System.out.println("[RDP] ⧗ Connecting to " + host + ":" + port);
        socket = new Socket(host, port);
        socket.setSoTimeout(READ_TIMEOUT);
        socket.setTcpNoDelay(true);
        
        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 65536));
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 65536));
        
        System.out.println("[RDP] ✓ TCP connection established");
    }
    
    private void step1_X224ConnectionRequest() throws IOException {
        System.out.println("[RDP] Step 1: Sending X.224 Connection Request...");
        
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(payload);
        
        dos.writeByte(0x01);
        dos.writeByte(0x00);
        dos.writeShort(0x0008);
        dos.writeInt(0x00000001);
        
        byte[] userData = payload.toByteArray();
        
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        DataOutputStream packetDos = new DataOutputStream(packet);
        
        packetDos.writeByte(0x03);
        packetDos.writeByte(0x00);
        packetDos.writeShort(0);
        packetDos.writeByte(0x02);
        packetDos.writeByte(0xF0);
        packetDos.writeByte(0x80);
        packetDos.write(userData);
        
        byte[] fullPacket = packet.toByteArray();
        fullPacket[2] = (byte) ((fullPacket.length >> 8) & 0xFF);
        fullPacket[3] = (byte) (fullPacket.length & 0xFF);
        
        if (DEBUG) {
            RDPPacketDebugger.printPacket("X.224 Connection Request", fullPacket);
        }
        
        outputStream.write(fullPacket);
        outputStream.flush();
        
        System.out.println("[RDP] ✓ Step 1 complete");
    }
    
    private void step2_X224ConnectionConfirm() throws IOException {
        System.out.println("[RDP] Step 2: Waiting for X.224 Connection Confirm...");
        
        byte[] tpktHeader = new byte[4];
        inputStream.readFully(tpktHeader);
        
        int tpktLength = ((tpktHeader[2] & 0xFF) << 8) | (tpktHeader[3] & 0xFF);
        byte[] data = new byte[tpktLength - 4];
        inputStream.readFully(data);
        
        byte[] fullResponse = new byte[tpktLength];
        System.arraycopy(tpktHeader, 0, fullResponse, 0, 4);
        System.arraycopy(data, 0, fullResponse, 4, data.length);
        
        if (DEBUG) {
            RDPPacketDebugger.printPacket("X.224 Connection Confirm", fullResponse);
        }
        
        System.out.println("[RDP] ✓ Step 2 complete");
    }
    
    private void step3_MCSConnectInitial() throws IOException {
        System.out.println("[RDP] Step 3: Sending MCS Connect Initial...");
        
        byte[] gccData = buildGCCConferenceCreateRequest();
        
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(packet);
        
        dos.writeByte(0x03);
        dos.writeByte(0x00);
        dos.writeShort(0);
        dos.writeByte(0x02);
        dos.writeByte(0xF0);
        dos.writeByte(0x80);
        dos.writeByte(0x7F);
        encodeLength(dos, gccData.length);
        dos.write(gccData);
        
        byte[] fullPacket = packet.toByteArray();
        fullPacket[2] = (byte) ((fullPacket.length >> 8) & 0xFF);
        fullPacket[3] = (byte) (fullPacket.length & 0xFF);
        
        if (DEBUG) {
            RDPPacketDebugger.printPacket("MCS Connect Initial", fullPacket);
        }
        
        outputStream.write(fullPacket);
        outputStream.flush();
        
        System.out.println("[RDP] ✓ Step 3 complete (GCC data: " + gccData.length + " bytes)");
    }
    
    private void step4_MCSConnectResponse() throws IOException {
        System.out.println("[RDP] Step 4: Waiting for MCS Connect Response...");
        
        byte[] tpktHeader = new byte[4];
        inputStream.readFully(tpktHeader);
        
        int tpktLength = ((tpktHeader[2] & 0xFF) << 8) | (tpktHeader[3] & 0xFF);
        byte[] data = new byte[tpktLength - 4];
        inputStream.readFully(data);
        
        byte[] fullResponse = new byte[tpktLength];
        System.arraycopy(tpktHeader, 0, fullResponse, 0, 4);
        System.arraycopy(data, 0, fullResponse, 4, data.length);
        
        if (DEBUG) {
            RDPPacketDebugger.printPacket("Server MCS Connect Response", fullResponse);
        }
        
        System.out.println("[RDP] ✓ Step 4 complete");
    }
    
    /**
     * 关键步骤：在发送 Attach User 之前，读取服务器的初始化数据
     * 包括 Server License Error、Server Demand Active 等
     */
    private void step4_5_ReadServerInitialData() throws IOException {
        System.out.println("[RDP] Step 4.5: Reading Server Initial Data...");
        
        socket.setSoTimeout(5000);
        
        int packetCount = 0;
        while (true) {
            try {
                byte[] tpktHeader = new byte[4];
                int read = inputStream.read(tpktHeader);
                
                if (read < 4) {
                    System.out.println("[RDP] ⊙ No more initial data");
                    break;
                }
                
                int tpktLength = ((tpktHeader[2] & 0xFF) << 8) | (tpktHeader[3] & 0xFF);
                byte[] data = new byte[tpktLength - 4];
                
                if (data.length > 0) {
                    inputStream.readFully(data);
                }
                
                packetCount++;
                System.out.println("[RDP] ⊙ Initial packet " + packetCount + ": " + data.length + " bytes");
                
                if (DEBUG && data.length > 0) {
                    // 显示前几字节以识别包类型
                    System.out.println("[RDP]   Packet data: " + 
                        String.format("%02X %02X %02X %02X", 
                            data[0] & 0xFF, 
                            data.length > 1 ? data[1] & 0xFF : 0,
                            data.length > 2 ? data[2] & 0xFF : 0,
                            data.length > 3 ? data[3] & 0xFF : 0));
                }
                
            } catch (java.net.SocketTimeoutException e) {
                System.out.println("[RDP] ⊙ Initial data read timeout (normal)");
                break;
            }
        }
        
        socket.setSoTimeout(READ_TIMEOUT);
        System.out.println("[RDP] ✓ Step 4.5 complete (" + packetCount + " packets)");
    }
    
    private void step5_AttachUserRequest() throws IOException {
        System.out.println("[RDP] Step 5: Sending Attach User Request...");
        
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(packet);
        
        dos.writeByte(0x03);
        dos.writeByte(0x00);
        dos.writeShort(0);
        dos.writeByte(0x02);
        dos.writeByte(0xF0);
        dos.writeByte(0x80);
        dos.writeByte(0x28);
        
        byte[] fullPacket = packet.toByteArray();
        fullPacket[2] = (byte) ((fullPacket.length >> 8) & 0xFF);
        fullPacket[3] = (byte) (fullPacket.length & 0xFF);
        
        if (DEBUG) {
            RDPPacketDebugger.printPacket("Attach User Request", fullPacket);
        }
        
        outputStream.write(fullPacket);
        outputStream.flush();
        
        System.out.println("[RDP] ✓ Step 5 complete");
    }
    
    private void step6_AttachUserResponse() throws IOException {
        System.out.println("[RDP] Step 6: Waiting for Attach User Response...");
        
        byte[] tpktHeader = new byte[4];
        int read = inputStream.read(tpktHeader);
        
        if (read < 4) {
            throw new IOException("Server closed connection before sending Attach User Response");
        }
        
        int tpktLength = ((tpktHeader[2] & 0xFF) << 8) | (tpktHeader[3] & 0xFF);
        byte[] data = new byte[tpktLength - 4];
        inputStream.readFully(data);
        
        if (data.length >= 6) {
            this.userId = ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
            System.out.println("[RDP] ⊙ User ID: " + userId);
        }
        
        byte[] fullResponse = new byte[tpktLength];
        System.arraycopy(tpktHeader, 0, fullResponse, 0, 4);
        System.arraycopy(data, 0, fullResponse, 4, data.length);
        
        if (DEBUG) {
            RDPPacketDebugger.printPacket("Attach User Response", fullResponse);
        }
        
        System.out.println("[RDP] ✓ Step 6 complete");
    }
    
    private void step7_ChannelJoins() throws IOException {
        System.out.println("[RDP] Step 7: Receiving Channel Join Confirms...");
        
        socket.setSoTimeout(3000);
        
        int channelCount = 0;
        while (true) {
            try {
                byte[] tpktHeader = new byte[4];
                int read = inputStream.read(tpktHeader);
                
                if (read < 4) {
                    break;
                }
                
                int tpktLength = ((tpktHeader[2] & 0xFF) << 8) | (tpktHeader[3] & 0xFF);
                byte[] data = new byte[tpktLength - 4];
                
                if (data.length > 0) {
                    inputStream.readFully(data);
                }
                
                channelCount++;
                System.out.println("[RDP] ⊙ Channel " + channelCount + " joined");
                
            } catch (java.net.SocketTimeoutException e) {
                break;
            }
        }
        
        socket.setSoTimeout(READ_TIMEOUT);
        System.out.println("[RDP] ✓ Step 7 complete (" + channelCount + " channels)");
    }
    
    private byte[] buildGCCConferenceCreateRequest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeByte(0x01);
        dos.writeByte(0x02);
        
        ByteArrayOutputStream descBaos = new ByteArrayOutputStream();
        DataOutputStream descDos = new DataOutputStream(descBaos);
        
        descDos.writeByte(0x04);
        descDos.writeByte(0x02);
        descDos.writeBytes("1\0");
        
        descDos.writeByte(0x34);
        descDos.writeByte(0x00);
        
        byte[] descData = descBaos.toByteArray();
        dos.writeByte(descData.length);
        dos.write(descData);
        
        dos.writeByte(0xA0);
        
        ByteArrayOutputStream connBaos = new ByteArrayOutputStream();
        DataOutputStream connDos = new DataOutputStream(connBaos);
        
        connDos.writeByte(0x02);
        
        ByteArrayOutputStream coreBaos = new ByteArrayOutputStream();
        DataOutputStream coreDos = new DataOutputStream(coreBaos);
        
        coreDos.writeInt(0x00080004);
        coreDos.writeInt(screenWidth);
        coreDos.writeInt(screenHeight);
        coreDos.writeShort((short) 0xCA01);
        coreDos.writeShort((short) 0x0001);
        coreDos.writeInt(0x00000409);
        coreDos.writeInt(0x00000A00);
        
        byte[] nameBytes = username.getBytes(StandardCharsets.UTF_16LE);
        coreDos.writeShort((short) nameBytes.length);
        coreDos.write(nameBytes);
        
        coreDos.writeInt(0x00000004);
        coreDos.writeInt(0x00000000);
        coreDos.writeInt(0x0000000C);
        coreDos.writeShort((short) 0);
        coreDos.writeShort((short) 0);
        coreDos.writeShort((short) 0);
        coreDos.writeInt(0x00000000);
        coreDos.writeInt(0x00000000);
        
        byte[] coreData = coreBaos.toByteArray();
        connDos.writeShort((short) coreData.length);
        connDos.write(coreData);
        
        connDos.writeByte(0x04);
        connDos.writeShort((short) 0x0C);
        connDos.writeInt(0x00000000);
        connDos.writeInt(0x00000000);
        
        connDos.writeByte(0x06);
        
        ByteArrayOutputStream netBaos = new ByteArrayOutputStream();
        DataOutputStream netDos = new DataOutputStream(netBaos);
        netDos.writeInt(0x00000000);
        
        byte[] netData = netBaos.toByteArray();
        connDos.writeShort((short) netData.length);
        connDos.write(netData);
        
        connDos.writeByte(0x09);
        connDos.writeShort((short) 0x0008);
        connDos.writeInt(0x00000000);
        
        connDos.writeByte(0x0A);
        connDos.writeShort((short) 0x08);
        connDos.writeInt(0x00000001);
        connDos.writeInt(0x00000000);
        connDos.writeInt(0x00000000);
        connDos.writeInt(screenWidth - 1);
        connDos.writeInt(screenHeight - 1);
        connDos.writeInt(0x00000001);
        
        byte[] connData = connBaos.toByteArray();
        encodeLength(dos, connData.length);
        dos.write(connData);
        
        return baos.toByteArray();
    }
    
    private void encodeLength(DataOutputStream dos, int length) throws IOException {
        if (length < 128) {
            dos.writeByte(length);
        } else if (length < 256) {
            dos.writeByte(0x81);
            dos.writeByte(length);
        } else {
            dos.writeByte(0x82);
            dos.writeByte((length >> 8) & 0xFF);
            dos.writeByte(length & 0xFF);
        }
    }
    
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected();
    }
    
    public void disconnect() throws IOException {
        connected = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            // Ignore
        }
        System.out.println("[RDP] ✓ Disconnected");
    }
    
    public DataInputStream getInputStream() {
        return inputStream;
    }
    
    public DataOutputStream getOutputStream() {
        return outputStream;
    }
    
    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
}