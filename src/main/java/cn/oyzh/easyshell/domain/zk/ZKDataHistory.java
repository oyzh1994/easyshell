package cn.oyzh.easyshell.domain.zk;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.Objects;

/**
 * zk数据历史
 *
 * @author oyzh
 * @since 2024/04/23
 */
@Table("t_zk_data_history")
public class ZKDataHistory implements ObjectComparator<ZKDataHistory>, ObjectCopier<ZKDataHistory>, Serializable {

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

    /**
     * 路径
     */
    @Column
    private String path;

    /**
     * 连接信息id
     *
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    @Column
    private String iid;

    @Override
    public boolean compare(ZKDataHistory t1) {
        if (t1 == null) {
            return false;
        }
        if (Objects.equals(this, t1)) {
            return true;
        }
        if (!Objects.equals(this.path, t1.path)) {
            return false;
        }
        if (!Objects.equals(this.dataLength, t1.dataLength)) {
            return false;
        }
        return Objects.equals(this.iid, t1.iid);
    }

    @Override
    public void copy(ZKDataHistory history) {
        this.data = history.data;
        this.path = history.path;
        this.saveTime = history.saveTime;
        this.dataLength = history.dataLength;
    }

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public void setData(byte[] data) {
        this.data = data;
        if (data != null) {
            this.dataLength = data.length;
        } else {
            this.dataLength = 0;
        }
    }
}
