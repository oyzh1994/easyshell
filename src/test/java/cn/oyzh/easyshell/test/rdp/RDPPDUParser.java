package cn.oyzh.easyshell.test.rdp;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * RDP PDU 数据包解析器
 * 支持解析各类 RDP 协议数据单元
 */
public class RDPPDUParser {
    
    // PDU 类型常量
    public static final int PDU_TYPE_DEMAND_ACTIVE = 0x01;
    public static final int PDU_TYPE_CONFIRM_ACTIVE = 0x03;
    public static final int PDU_TYPE_DEACTIVATE_ALL = 0x06;
    public static final int PDU_TYPE_DATA = 0x07;
    public static final int PDU_TYPE_SERVER_REDIRECT = 0x0A;
    
    // Server Update SDATA Types
    public static final int SDATA_TYPE_PALETTE = 0x0A;
    public static final int SDATA_TYPE_BITMAP = 0x01;
    public static final int SDATA_TYPE_SYNCHRONIZE = 0x1F;
    
    private DataInputStream inputStream;
    
    public RDPPDUParser(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    /**
     * 读取下一个 RDP 数据包
     */
    public RDPPacket readPacket() throws IOException {
        RDPPacket packet = new RDPPacket();
        
        // 读取 TPKT 头 (4 字节)
        byte version = inputStream.readByte();
        byte reserved = inputStream.readByte();
        short tpktLength = readUInt16LE();
        
        if (version != 0x03) {
            throw new IOException("Invalid TPKT version: " + version);
        }
        
        // 读取 TPKT 数据
        byte[] tpktData = new byte[tpktLength - 4];
        inputStream.readFully(tpktData);
        
        ByteBuffer buffer = ByteBuffer.wrap(tpktData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // 解析 RDP 头
        parseRDPHeader(packet, buffer);
        
        return packet;
    }
    
    private void parseRDPHeader(RDPPacket packet, ByteBuffer buffer) {
        // X.224 COTP Data PDU
        byte code = buffer.get();
        byte eot = buffer.get();
        
        // X.224/ISO 8859-1 headers
        int headerLen = 0;
        while (buffer.hasRemaining() && headerLen < 100) {
            byte b = buffer.get();
            if (b == 0) break;
            headerLen++;
        }
        
        // RDP 序列号和压缩标志
        if (buffer.hasRemaining()) {
            byte flags = buffer.get();
            packet.setFlags(flags);
        }
        
        if (buffer.hasRemaining()) {
            short secFlags = readUInt16LE(buffer);
            packet.setSecurityFlags(secFlags);
        }
        
        // 剩余数据为 PDU 数据
        byte[] pduData = new byte[buffer.remaining()];
        buffer.get(pduData);
        packet.setPduData(pduData);
    }
    
    /**
     * 解析 Server Update (位图更新)
     */
    public BitmapUpdate parseBitmapUpdate(byte[] updateData) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(updateData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        BitmapUpdate bitmapUpdate = new BitmapUpdate();
        
        // Update Header
        short updateType = buffer.getShort();
        bitmapUpdate.setUpdateType(updateType);
        
        if (updateType == SDATA_TYPE_BITMAP) {
            // Bitmap Update Structure
            short count = buffer.getShort();  // Bitmap count
            
            for (int i = 0; i < count; i++) {
                BitmapData bitmap = parseBitmapData(buffer);
                bitmapUpdate.addBitmap(bitmap);
            }
        }
        
        return bitmapUpdate;
    }
    
    private BitmapData parseBitmapData(ByteBuffer buffer) {
        BitmapData bitmap = new BitmapData();
        
        // Bitmap Header
        short destLeft = buffer.getShort();
        short destTop = buffer.getShort();
        short destRight = buffer.getShort();
        short destBottom = buffer.getShort();
        
        int width = destRight - destLeft + 1;
        int height = destBottom - destTop + 1;
        
        bitmap.setX(destLeft);
        bitmap.setY(destTop);
        bitmap.setWidth(width);
        bitmap.setHeight(height);
        
        short bitsPerPixel = buffer.getShort();
        bitmap.setBitsPerPixel(bitsPerPixel);
        
        short flags = buffer.getShort();
        bitmap.setFlags(flags);
        
        int bitmapLength = buffer.getInt();
        
        // 位图数据
        byte[] bitmapDataBytes = new byte[bitmapLength];
        buffer.get(bitmapDataBytes);
        bitmap.setData(bitmapDataBytes);
        
        return bitmap;
    }
    
    private short readUInt16LE() throws IOException {
        byte[] bytes = new byte[2];
        inputStream.readFully(bytes);
        return (short) ((bytes[1] << 8) | (bytes[0] & 0xFF));
    }
    
    private short readUInt16LE(ByteBuffer buffer) {
        return buffer.getShort();
    }
}