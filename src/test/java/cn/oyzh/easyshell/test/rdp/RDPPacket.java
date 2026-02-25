package cn.oyzh.easyshell.test.rdp;

/**
 * RDP 数据包类
 */
public class RDPPacket {
    private byte flags;
    private short securityFlags;
    private byte[] pduData;
    
    // Getters and Setters
    public byte getFlags() { return flags; }
    public void setFlags(byte flags) { this.flags = flags; }
    
    public short getSecurityFlags() { return securityFlags; }
    public void setSecurityFlags(short securityFlags) { this.securityFlags = securityFlags; }
    
    public byte[] getPduData() { return pduData; }
    public void setPduData(byte[] pduData) { this.pduData = pduData; }
}

