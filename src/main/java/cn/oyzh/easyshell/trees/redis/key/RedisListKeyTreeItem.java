package cn.oyzh.easyshell.trees.redis.key;

import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.redis.key.ShellRedisListValue;
import cn.oyzh.fx.plus.information.MessageBox;

import java.util.List;

/**
 * @author oyzh
 * @since 2023/06/30
 */
public class RedisListKeyTreeItem extends RedisRowKeyTreeItem<ShellRedisListValue.RedisListRow> {

    public RedisListKeyTreeItem(ShellRedisKey value, RedisDatabaseTreeItem dbItem) {
        super(value, dbItem);
    }

    @Override
    public ShellRedisListValue.RedisListRow data() {
        return (ShellRedisListValue.RedisListRow) super.data();
    }

    @Override
    public void data(Object data) {
        if (data instanceof ShellRedisListValue.RedisListRow row) {
            super.data(row.clone());
        } else {
            super.clearData();
        }
    }

    @Override
    public ShellRedisListValue.RedisListRow unsavedValue() {
        return (ShellRedisListValue.RedisListRow) super.unsavedValue();
    }

    @Override
    public void saveKeyValue() {
        ShellRedisListValue.RedisListRow row = this.data();
        try {
            if (row != null) {
                // 更新数据
                this.setKeyValue(row);
                // 更新当前行
                this.currentRow.setValue(row.getValue());
                // 清除数据
                this.clearData();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void setKeyValue(Object value) {
        // 更新数据
        if (value instanceof ShellRedisListValue.RedisListRow row) {
            this.client().lset(this.dbIndex(), this.key(), row.getIndex() - 1, row.getValue());
        }
    }

    @Override
    public boolean deleteRow() {
        try {
            long count = this.client().lrem(this.dbIndex(), this.key(), this.currentRow.getValue());
            if (count > 0) {
                this.rows().remove(this.currentRow);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return false;
    }

    @Override
    public boolean reloadRow() {
        String value = this.client().lindex(this.dbIndex(), this.key(), this.currentRow.getIndex() - 1);
        if (value != null) {
            this.currentRow.setValue(value);
            return true;
        }
        return false;
    }

    @Override
    public void refreshKeyValue() {
        try {
            // 更新数据
            List<String> value = this.client().lrange(this.dbIndex(), this.key());
            this.value.valueOfList(value);
            // 清空未保存的数据
            this.clearData();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public ShellRedisListValue.RedisListRow rawValue() {
        return this.currentRow;
    }

    @Override
    public boolean isDataTooBig() {
        Object o = this.data();
        if (o instanceof ShellRedisListValue.RedisListRow r) {
            String s = r.getValue();
            if (s.length() > DATA_MAX) {
                return true;
            }
            return s.lines().anyMatch(l -> l.length() > LINE_MAX);
        }
        return false;
    }
}
