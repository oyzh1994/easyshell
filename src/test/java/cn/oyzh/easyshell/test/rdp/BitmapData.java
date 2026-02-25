package cn.oyzh.easyshell.test.rdp;

/**
 * 位图数据
 */
public class BitmapData {
    private int x, y, width, height;
    private int bitsPerPixel;
    private short flags;
    private byte[] data;
    
    // Getters and Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    
    public int getBitsPerPixel() { return bitsPerPixel; }
    public void setBitsPerPixel(int bitsPerPixel) { this.bitsPerPixel = bitsPerPixel; }
    
    public short getFlags() { return flags; }
    public void setFlags(short flags) { this.flags = flags; }
    
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
}
