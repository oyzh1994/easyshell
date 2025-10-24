package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.easyshell.redis.key.ShellRedisJsonValue;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.fx.plus.information.MessageBox;

/**
 * @author oyzh
 * @since 2023/06/30
 */
public class ShellRedisJsonKeyTreeItem extends ShellRedisKeyTreeItem {

    public ShellRedisJsonKeyTreeItem(ShellRedisKey value, ShellRedisDatabaseTreeItem dbItem) {
        super(value, dbItem);
    }

    @Override
    public void saveKeyValue() {
        Object value = this.data();
        try {
            if (value != null) {
                this.setKeyValue(value);
                // 更新值
                if (value instanceof String s) {
                    this.keyValue().setValue(s);
                } else if (value instanceof byte[] bytes) {
                    this.keyValue().setValue(new String(bytes));
                }
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
            this.client().jsonSet(this.dbIndex(), this.key(), string);
        } else if (value instanceof byte[] bytes) {
            this.client().jsonSet(this.dbIndex(), this.key(), new String(bytes));
        }
    }

    @Override
    public void refreshKeyValue() {
        try {
            String val = this.client().jsonGet(this.dbIndex(), this.key());
            this.value.valueOfJson(val);
            // 清空未保存的数据
            this.clearData();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public Object rawValue() {
        ShellRedisJsonValue rawValue = this.value.asJsonValue();
        if (rawValue == null || !rawValue.hasValue()) {
            // 刷新值
            this.refreshKeyValue();
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

    @Override
    public ShellRedisJsonValue keyValue() {
        return (ShellRedisJsonValue) super.keyValue();
    }

    @Override
    public Object rawData() {
        return this.rawValue();
    }
}
