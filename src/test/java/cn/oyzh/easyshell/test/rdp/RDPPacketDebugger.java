package cn.oyzh.easyshell.test.rdp;

import java.io.*;

/**
 * RDP 包调试工具 - 用于分析发送/接收的数据
 */
public class RDPPacketDebugger {
    
    private static final int BYTES_PER_LINE = 16;
    
    public static String hexDump(byte[] data) {
        return hexDump(data, 0, data.length);
    }
    
    public static String hexDump(byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i += BYTES_PER_LINE) {
            // 偏移量
            sb.append(String.format("%04X: ", offset + i));
            
            // Hex 值
            for (int j = 0; j < BYTES_PER_LINE; j++) {
                if (i + j < length) {
                    sb.append(String.format("%02X ", data[offset + i + j] & 0xFF));
                } else {
                    sb.append("   ");
                }
            }
            
            sb.append(" | ");
            
            // ASCII 值
            for (int j = 0; j < BYTES_PER_LINE; j++) {
                if (i + j < length) {
                    byte b = data[offset + i + j];
                    if (b >= 32 && b < 127) {
                        sb.append((char) b);
                    } else {
                        sb.append(".");
                    }
                } else {
                    sb.append(" ");
                }
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    public static void printPacket(String name, byte[] data) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PACKET: " + name + " (" + data.length + " bytes)");
        System.out.println("=".repeat(80));
        System.out.print(hexDump(data));
        System.out.println("=".repeat(80) + "\n");
    }
    
    /**
     * 解析 TPKT 头
     */
    public static void analyzeTpkt(byte[] data) {
        if (data.length < 4) {
            System.out.println("[TPKT] Invalid: too short");
            return;
        }
        
        byte version = data[0];
        byte reserved = data[1];
        int length = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        
        System.out.println("[TPKT] Version: " + (version & 0xFF));
        System.out.println("[TPKT] Reserved: " + (reserved & 0xFF));
        System.out.println("[TPKT] Length: " + length);
    }
    
    /**
     * 解析 X.224 头
     */
    public static void analyzeX224(byte[] data) {
        if (data.length < 7) {
            System.out.println("[X.224] Invalid: too short");
            return;
        }
        
        byte li = data[4];
        byte type = data[5];
        byte routing = data[6];
        
        System.out.println("[X.224] LI: " + (li & 0xFF));
        System.out.println("[X.224] Type: " + (type & 0xFF) + 
                          (type == 0xF0 ? " (DT TPDU)" : ""));
        System.out.println("[X.224] Routing: " + (routing & 0xFF));
    }
}