package cn.oyzh.easyshell.test.rdp;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

/**
 * RDP 连接修复版 v2
 * 正确处理 MCS 握手流程，包括所有必需的通道连接
 */
public class RDPConnection0 {
    
    private static final int DEFAULT_RDP_PORT = 3389;
    private static final int READ_TIMEOUT = 10000;
    
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    
    private String host;
    private int port;
    private String username;
    private String password;
    
    private volatile boolean connected = false;
    private RDPSecurityLayer securityLayer;
    
    private int screenWidth = 1024;
    private int screenHeight = 768;
    
    public RDPConnection0(String host, int port, String username, String password) {
        this.host = host;
        this.port = port > 0 ? port : DEFAULT_RDP_PORT;
        this.username = username != null ? username : "user";
        this.password = password != null ? password : "";
        this.securityLayer = new RDPSecurityLayer();
    }

    public void connect() throws Exception {
        try {
            connectSocket();
            sendX224ConnectionRequest();
            readX224ConnectionConfirm(); // 现在会设置 selectedProtocol

            // 如果服务器要求 TLS (CredSSP)，进行握手
            if (selectedProtocol == 0x00000002) {
                System.out.println("[RDP] ⧗ Server requires TLS, starting TLS handshake...");
                performTlsHandshake();
                System.out.println("[RDP] ✓ TLS handshake completed");
            } else if (selectedProtocol == 0x00000001) {
                System.out.println("[RDP] ⊙ Using standard RDP encryption (not implemented)");
                // 这里可以添加 RDP 加密层处理，但通常与 TLS 不同
            } else {
                System.out.println("[RDP] ⊙ No encryption requested");
            }

            sendClientMCSConnectInitial();
            receiveServerMCSConnect();
            sendClientMCSConnectResponse();
            receiveAndProcessServerPackets();

            connected = true;
            System.out.println("[RDP] ✓ Connected successfully!");
        } catch (Exception e) {
            System.err.println("[RDP] ✗ Connection failed: " + e.getMessage());
            e.printStackTrace();
            disconnect();
            throw e;
        }
    }

    private void performTlsHandshake() throws Exception {
        // 创建信任所有证书的 TrustManager
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // 将现有 Socket 升级为 SSLSocket
        SSLSocketFactory sslFactory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(socket, host, port, true);
        sslSocket.setSoTimeout(READ_TIMEOUT);
        sslSocket.setTcpNoDelay(true);
        sslSocket.startHandshake();

        // 替换输入输出流
        this.socket = sslSocket;
        this.inputStream = new DataInputStream(new BufferedInputStream(sslSocket.getInputStream(), 65536));
        this.outputStream = new DataOutputStream(new BufferedOutputStream(sslSocket.getOutputStream(), 65536));

        // 通知安全层（如果后续需要加密处理）
//        securityLayer.setTlsEnabled(true);
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
    
    private void sendX224ConnectionRequest() throws IOException {
        System.out.println("[RDP] ⧗ Sending X.224 Connection Request...");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeByte(0x03);                    // TPKT Version
        dos.writeByte(0x00);                    // Reserved
        dos.writeShort(0);                      // Length (待填充)
        dos.writeByte(0x02);                    // X.224 LI
        dos.writeByte(0xF0);                    // X.224 Type
        dos.writeByte(0x80);                    // X.224 Routing
        dos.writeByte(0x01);                    // RDP Negotiation Request Type
        dos.writeByte(0x00);                    // Flags
        dos.writeShort(0x0008);                 // Length
        dos.writeInt(0x00000001);               // Requested Protocols = RDP
        
        byte[] data = baos.toByteArray();
        data[2] = (byte) ((data.length >> 8) & 0xFF);
        data[3] = (byte) (data.length & 0xFF);
        
        outputStream.write(data);
        outputStream.flush();
        
        System.out.println("[RDP] ✓ X.224 Connection Request sent");
    }
    
    private void sendClientMCSConnectInitial() throws IOException {
        System.out.println("[RDP] ⧗ Sending Client MCS Connect Initial...");
        
        byte[] gccData = buildGCCConferenceCreateRequest();
        System.out.println("[RDP] ⊙ GCC data size: " + gccData.length + " bytes");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeByte(0x03);                    // TPKT Version
        dos.writeByte(0x00);                    // Reserved
        dos.writeShort(0);                      // Length (待填充)
        dos.writeByte(0x02);                    // X.224 LI
        dos.writeByte(0xF0);                    // X.224 Type
        dos.writeByte(0x80);                    // X.224 Routing
        dos.writeByte(0x7F);                    // MCS Tag
        dos.writeShort(gccData.length);         // Length of GCC data
        dos.write(gccData);
        
        byte[] data = baos.toByteArray();
        data[2] = (byte) ((data.length >> 8) & 0xFF);
        data[3] = (byte) (data.length & 0xFF);
        
        outputStream.write(data);
        outputStream.flush();
        
        System.out.println("[RDP] ✓ Client MCS Connect Initial sent (" + data.length + " bytes)");
    }
    
    /**
     * 接收服务器的 MCS Connect 响应和所有相关的初始数据
     * 这包括 MCS Connect Response, Attach User Confirm, Channel Join 等
     */
    private void receiveServerMCSConnect() throws IOException {
        System.out.println("[RDP] ⧗ Receiving Server MCS Connect data...");
        
        int packetCount = 0;
        int totalBytes = 0;
        
        // 接收至少 1 个包
        byte[] tpktHeader = new byte[4];
        inputStream.readFully(tpktHeader);
        
        int tpktLength = ((tpktHeader[2] & 0xFF) << 8) | (tpktHeader[3] & 0xFF);
        byte[] data = new byte[tpktLength - 4];
        inputStream.readFully(data);
        
        packetCount++;
        totalBytes += data.length;
        System.out.println("[RDP] ⊙ Packet " + packetCount + ": " + data.length + " bytes");
        
        // 可能还有更多包，设置短超时继续读取
        socket.setSoTimeout(1000);
        
        while (true) {
            try {
                byte[] nextHeader = new byte[4];
                int read = inputStream.read(nextHeader);
                
                if (read < 4) {
                    break;  // 连接关闭或没有更多数据
                }
                
                int nextLength = ((nextHeader[2] & 0xFF) << 8) | (nextHeader[3] & 0xFF);
                byte[] nextData = new byte[nextLength - 4];
                
                if (nextData.length > 0) {
                    inputStream.readFully(nextData);
                }
                
                packetCount++;
                totalBytes += nextData.length;
                System.out.println("[RDP] ⊙ Packet " + packetCount + ": " + nextData.length + " bytes");
                
            } catch (SocketTimeoutException e) {
                break;  // 没有更多包，正常
            }
        }
        
        // 恢复正常超时
        socket.setSoTimeout(READ_TIMEOUT);
        
        System.out.println("[RDP] ✓ Received " + packetCount + " packets (" + 
                          totalBytes + " bytes total)");
    }
    
    private void sendClientMCSConnectResponse() throws IOException {
        System.out.println("[RDP] ⧗ Sending Client MCS Connect Response...");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeByte(0x03);
        dos.writeByte(0x00);
        dos.writeShort(0);
        dos.writeByte(0x02);
        dos.writeByte(0xF0);
        dos.writeByte(0x80);
        dos.writeByte(0x7F);
        dos.writeShort(0x0002);
        
        byte[] data = baos.toByteArray();
        data[2] = (byte) ((data.length >> 8) & 0xFF);
        data[3] = (byte) (data.length & 0xFF);
        
        outputStream.write(data);
        outputStream.flush();
        
        System.out.println("[RDP] ✓ Client MCS Connect Response sent");
    }

    private int selectedProtocol = 0; // 成员变量

    private void readX224ConnectionConfirm() throws IOException {
        System.out.println("[RDP] ⧗ Waiting for X.224 Connection Confirm...");

        byte[] tpktHeader = new byte[4];
        inputStream.readFully(tpktHeader);

        int tpktLength = ((tpktHeader[2] & 0xFF) << 8) | (tpktHeader[3] & 0xFF);
        byte[] data = new byte[tpktLength - 4];
        inputStream.readFully(data);

        System.out.println("[RDP] ✓ X.224 Connection Confirm received (" + data.length + " bytes)");

        // 解析 RDP Negotiation Response (位于 X.224 TPDU 之后)
        // X.224 TPDU 结构: LI(1) + 类型(1) + 路由(1) = 3 字节 (LI=2 时)
        int x224HeaderSize = 1 + (data[0] & 0xFF); // LI 字段表示后续字节数，总头长度 = 1 + LI
        if (data.length >= x224HeaderSize + 8) { // 至少包含 RDP Negotiation Response (8字节)
            int rdpNegType = data[x224HeaderSize] & 0xFF;
            if (rdpNegType == 0x02) { // RDP Negotiation Response
                int flags = data[x224HeaderSize + 1] & 0xFF;
                int length = ((data[x224HeaderSize + 2] & 0xFF) << 8) | (data[x224HeaderSize + 3] & 0xFF);
                selectedProtocol = ((data[x224HeaderSize + 4] & 0xFF) << 24) |
                        ((data[x224HeaderSize + 5] & 0xFF) << 16) |
                        ((data[x224HeaderSize + 6] & 0xFF) << 8) |
                        (data[x224HeaderSize + 7] & 0xFF);
                System.out.println("[RDP] ⊙ RDP Negotiation Response: type=" + rdpNegType +
                        ", flags=" + flags + ", length=" + length +
                        ", selectedProtocol=0x" + Integer.toHexString(selectedProtocol));
            }
        }
    }
    
    /**
     * 接收并处理服务器的后续包（Attach User, Channel Join 等）
     */
    private void receiveAndProcessServerPackets() throws IOException {
        System.out.println("[RDP] ⧗ Receiving Server Attach User and Channel data...");
        
        socket.setSoTimeout(2000);
        
        int packetCount = 0;
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
                
                packetCount++;
                System.out.println("[RDP] ⊙ Received packet " + packetCount + ": " + 
                                  data.length + " bytes");
                
            } catch (SocketTimeoutException e) {
                break;
            }
        }
        
        socket.setSoTimeout(READ_TIMEOUT);
        
        System.out.println("[RDP] ✓ Received " + packetCount + " channel packets");
    }
    
    /**
     * 构建 GCC Conference Create Request
     */
    private byte[] buildGCCConferenceCreateRequest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeByte(0x01);                    // Type: Conference Create Request
        
        // Conference Descriptor
        dos.writeByte(0x02);                    // Descriptor Tag
        
        ByteArrayOutputStream descBaos = new ByteArrayOutputStream();
        DataOutputStream descDos = new DataOutputStream(descBaos);
        
        descDos.writeByte(0x04);                // Tag: OctetString
        descDos.writeByte(0x02);                // Length
        descDos.writeByte('1');
        descDos.writeByte(0);
        
        descDos.writeByte(0x34);                // Tag: Sequence
        descDos.writeByte(0x00);                // Length (empty)
        
        byte[] descData = descBaos.toByteArray();
        dos.writeByte(descData.length);
        dos.write(descData);
        
        // Connection Data
        dos.writeByte(0xA0);                    // Tag [0]
        
        ByteArrayOutputStream connBaos = new ByteArrayOutputStream();
        DataOutputStream connDos = new DataOutputStream(connBaos);
        
        // Client Core Data
        connDos.writeByte(0x02);                // Type
        
        ByteArrayOutputStream coreBaos = new ByteArrayOutputStream();
        DataOutputStream coreDos = new DataOutputStream(coreBaos);
        
        coreDos.writeInt(0x00080004);           // Version
        coreDos.writeInt(screenWidth);
        coreDos.writeInt(screenHeight);
        coreDos.writeShort((short) 0xCA01);     // Color Depth
        coreDos.writeShort((short) 0x0001);     // SAS Sequence
        coreDos.writeInt(0x00000409);           // Keyboard Layout
        coreDos.writeInt(0x00000A00);           // Build
        
        byte[] nameBytes = encodeUCS2LE(username);
        coreDos.writeShort(nameBytes.length);
        coreDos.write(nameBytes);
        
        coreDos.writeInt(0x00000004);           // Keyboard Type
        coreDos.writeInt(0x00000000);           // Keyboard Subtype
        coreDos.writeInt(0x0000000C);           // Keyboard Function Key Count
        coreDos.writeShort((short) 0);          // IME File Name Length
        coreDos.writeShort((short) 0);
        coreDos.writeShort((short) 0);          // LCD Color Depth
        coreDos.writeInt(0x00000000);           // Server Selected Protocol
        coreDos.writeInt(0x00000000);           // Time Zone Bias
        
        byte[] coreData = coreBaos.toByteArray();
        connDos.writeShort(coreData.length);
        connDos.write(coreData);
        
        // Client Security Data
        connDos.writeByte(0x04);                // Type
        connDos.writeShort((short) 0x0C);       // Length
        connDos.writeInt(0x00000000);           // Encryption Methods
        connDos.writeInt(0x00000000);           // Extended Encryption Methods
        
        // Client Network Data
        connDos.writeByte(0x06);                // Type
        
        ByteArrayOutputStream netBaos = new ByteArrayOutputStream();
        DataOutputStream netDos = new DataOutputStream(netBaos);
        netDos.writeInt(0x00000000);            // Channel Count
        
        byte[] netData = netBaos.toByteArray();
        connDos.writeShort(netData.length);
        connDos.write(netData);
        
        // Client Cluster Data
        connDos.writeByte(0x09);                // Type
        connDos.writeShort((short) 0x0008);     // Length
        connDos.writeInt(0x00000000);           // Cluster Flags
        
        // Client Monitor Data
        connDos.writeByte(0x0A);                // Type
        connDos.writeShort((short) 0x08);       // Length
        connDos.writeInt(0x00000001);           // Monitor Count
        connDos.writeInt(0x00000000);           // Left
        connDos.writeInt(0x00000000);           // Top
        connDos.writeInt(screenWidth - 1);      // Right
        connDos.writeInt(screenHeight - 1);     // Bottom
        connDos.writeInt(0x00000001);           // Primary
        
        byte[] connData = connBaos.toByteArray();
        dos.writeShort(connData.length);
        dos.write(connData);
        
        return baos.toByteArray();
    }
    
    private byte[] encodeUCS2LE(String str) {
        return str.getBytes(StandardCharsets.UTF_16LE);
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
    
    public RDPSecurityLayer getSecurityLayer() {
        return securityLayer;
    }
    
    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
}