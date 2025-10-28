package cn.oyzh.easyshell.dto.zk;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.store.jdbc.Column;
import com.alibaba.fastjson2.annotation.JSONField;

/**
 * zk数据历史
 *
 * @author oyzh
 * @since 2024/04/23
 */
public class ShellZKHistoryData {

    /**
     * 内容
     */
    @Column
    private byte[] data;

    /**
     * 数据大小
     */
    @Column
    private long dataLength;

    /**
     * 保存时间
     */
    @Column
    private long saveTime = System.currentTimeMillis();

    @JSONField(serialize = false, deserialize = false)
    public String getDataSize() {
        long length = this.dataLength;
        if (length < 1024) {
            return length + "b";
        }
        if (length < 1024 * 1024) {
            return this.dataLength / 1024 + "Kb";
        }
        if (length < 1024 * 1024 * 1024) {
            return this.dataLength / 1024 / 1024 + "Mb";
        }
        return this.dataLength / 1024 / 1024 / 1024 + "Gb";
    }

    public byte[] getData() {
        return data;
    }

    public long getDataLength() {
        return dataLength;
    }

    public void setDataLength(long dataLength) {
        this.dataLength = dataLength;
    }

    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    public void setData(byte[] data) {
        this.data = data;
        if (data != null) {
            this.dataLength = data.length;
        } else {
            this.dataLength = 0;
        }
    }

    /**
     * 获取格式化的保存时间
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public String getSaveTimeFormated() {
        return DateUtil.format(this.getSaveTime(), "yy-MM-dd HH:mm:ss");
    }
}
