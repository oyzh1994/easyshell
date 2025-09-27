package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.redis.key.ShellRedisStringValue;
import cn.oyzh.fx.plus.information.MessageBox;

/**
 * @author oyzh
 * @since 2023/06/30
 */
public class ShellRedisStringKeyTreeItem extends ShellRedisKeyTreeItem {

    public ShellRedisStringKeyTreeItem(ShellRedisKey value, ShellRedisDatabaseTreeItem dbItem) {
        super(value, dbItem);
    }

    @Override
    public void saveKeyValue() {
        Object value = this.data();
        try {
            if (value != null) {
                this.setKeyValue(value);
                // 更新值
                this.keyValue().setValue(value);
                // 刷新统计值
                this.flushCount();
                // 清除缓存
                this.clearData();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void setKeyValue(Object value) {
        if (value instanceof String string) {
            this.client().set(this.dbIndex(), this.key(), string);
        } else if (value instanceof byte[] bytes) {
            this.client().set(this.dbIndex(), this.keyBinary(), bytes);
        }
    }

    @Override
    public void refreshKeyValue() {
        try {
            // 原始格式
            if (this.isRawEncoding(true)) {
                byte[] val = this.client().get(this.dbIndex(), this.keyBinary());
                this.value.valueOfBytes(val);
            } else {// 字符串格式
                String val = this.client().get(this.dbIndex(), this.key());
                this.value.valueOfString(val);
            }
            // 刷新统计值
            this.flushCount();
            // 清空未保存的数据
            this.clearData();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public Object rawValue() {
        ShellRedisStringValue rawValue = this.value.asStringValue();
        if (rawValue == null || !rawValue.hasValue()) {
            // 刷新值
            this.refreshKeyValue();
            // // 刷新统计值
            // this.flushCount();
        }
        return this.keyValue().getValue();
    }

    /**
     * 数据是否太大
     *
     * @return 结果
     */
    public boolean isDataTooBig() {
        Object o = this.data();
        if (o instanceof String s) {
            if (s.length() > DATA_MAX) {
                return true;
            }
            return s.lines().anyMatch(l -> l.length() > LINE_MAX);
        }
        if (o instanceof byte[] bytes) {
            if (bytes.length > DATA_MAX) {
                return true;
            }
            return new String(bytes).lines().anyMatch(l -> l.length() > LINE_MAX);
        }
        return false;
    }

    /**
     * 获取统计值大小
     *
     * @return 统计值大小
     */
    public boolean isHyLog() {
        ShellRedisStringValue stringValue = this.value.asStringValue();
        if (stringValue.getHyLog() == null) {
            this.flushCount();
        }
        return stringValue.isHyLog();
    }

    /**
     * 获取统计值
     *
     * @return 统计值
     */
    public Long count() {
        ShellRedisStringValue stringValue = this.value.asStringValue();
        if (stringValue.getCount() == null) {
            this.flushCount();
        }
        return stringValue.getCount();
    }

    /**
     * 刷新统计值
     */
    public void flushCount() {
        ShellRedisStringValue stringValue = this.value.asStringValue();
        try {
            stringValue.setCount(this.client().pfcount(this.dbIndex(), this.key()));
            stringValue.setHyLog(true);
        } catch (Exception ex) {
            if (StringUtil.containsAny(ex.getMessage(), "WRONGTYPE Key is not a valid HyperLogLog string value")) {
                stringValue.setHyLog(false);
            } else {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public ShellRedisStringValue keyValue() {
        return (ShellRedisStringValue) super.keyValue();
    }

    @Override
    public Object rawData() {
        return this.rawValue();
    }
}
