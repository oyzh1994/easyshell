package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.easyshell.redis.key.RedisKey;
import cn.oyzh.easyshell.redis.key.RedisListValue;
import cn.oyzh.fx.plus.information.MessageBox;

import java.util.List;

/**
 * @author oyzh
 * @since 2023/06/30
 */
public class RedisListKeyTreeItem extends RedisRowKeyTreeItem<RedisListValue.RedisListRow> {

    public RedisListKeyTreeItem(RedisKey value, RedisDatabaseTreeItem dbItem) {
        super(value, dbItem);
    }

    @Override
    public RedisListValue.RedisListRow data() {
        return (RedisListValue.RedisListRow) super.data();
    }

    @Override
    public void data(Object data) {
        if (data instanceof RedisListValue.RedisListRow row) {
            super.data(row.clone());
        } else {
            super.clearData();
        }
    }

    @Override
    public RedisListValue.RedisListRow unsavedValue() {
        return (RedisListValue.RedisListRow) super.unsavedValue();
    }

    @Override
    public void saveKeyValue() {
        RedisListValue.RedisListRow row = this.data();
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
        if (value instanceof RedisListValue.RedisListRow row) {
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
    public RedisListValue.RedisListRow rawValue() {
        return this.currentRow;
    }

    @Override
    public boolean isDataTooBig() {
        Object o = this.data();
        if (o instanceof RedisListValue.RedisListRow r) {
            String s = r.getValue();
            if (s.length() > DATA_MAX) {
                return true;
            }
            return s.lines().anyMatch(l -> l.length() > LINE_MAX);
        }
        return false;
    }
}
