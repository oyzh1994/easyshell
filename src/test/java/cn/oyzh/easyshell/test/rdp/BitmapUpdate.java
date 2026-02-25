package cn.oyzh.easyshell.test.rdp;

import java.util.ArrayList;
import java.util.List; /**
 * 位图更新
 */
public class BitmapUpdate {
    private short updateType;
    private List<BitmapData> bitmaps = new ArrayList<>();
    
    public short getUpdateType() { return updateType; }
    public void setUpdateType(short updateType) { this.updateType = updateType; }
    
    public List<BitmapData> getBitmaps() { return bitmaps; }
    public void addBitmap(BitmapData bitmap) { bitmaps.add(bitmap); }
}
