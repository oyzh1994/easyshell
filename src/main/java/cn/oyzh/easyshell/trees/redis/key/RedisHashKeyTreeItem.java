package cn.oyzh.easyshell.trees.redis.key;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisHashValue;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.fx.plus.information.MessageBox;

import java.util.Map;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2023/06/30
 */
public class RedisHashKeyTreeItem extends RedisRowKeyTreeItem<ShellRedisHashValue.RedisHashRow> {

    public RedisHashKeyTreeItem(ShellRedisKey value, RedisDatabaseTreeItem dbItem) {
        super(value, dbItem);
    }

    @Override
    public ShellRedisHashValue.RedisHashRow data() {
        return (ShellRedisHashValue.RedisHashRow) super.data();
    }

    @Override
    public void data(Object data) {
        if (data instanceof ShellRedisHashValue.RedisHashRow row) {
            super.data(row.clone());
        } else {
            super.clearData();
        }
    }

    @Override
    public ShellRedisHashValue.RedisHashRow unsavedValue() {
        return (ShellRedisHashValue.RedisHashRow) super.unsavedValue();
    }

    /**
     * 获取字段
     *
     * @return 字段
     */
    public String field() {
        if (this.data() == null) {
            return null;
        }
        return this.data().getField();
    }

    /**
     * 设置字段
     *
     * @param field 字段
     */
    public void field(String field) {
        if (this.data() != null) {
            this.data().setField(field);
        }
    }

    @Override
    public boolean checkRowExists() {
        String field1 = this.field();
        String field2 = this.currentRow.getField();
        if (field1 != null && !Objects.equals(field1, field2)) {
            return this.client().hexists(this.dbIndex(), this.key(), field1);
        }
        return false;
    }

    @Override
    public void saveKeyValue() {
        ShellRedisHashValue.RedisHashRow row = this.data();
        try {
            // 保存数据
            this.setKeyValue(row);
            // 更新行
            this.currentRow.setField(row.getField());
            this.currentRow.setValue(row.getValue());
            // 清除数据
            this.clearData();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void setKeyValue(Object value) {
        if (value instanceof ShellRedisHashValue.RedisHashRow row) {
            String field = row.getField();
            String oldField = this.currentRow.getField();
            this.client().hset(this.dbIndex(), this.key(), field, row.getValue());
            // 删除旧字段
            if (!StringUtil.equals(field, oldField)) {
                this.client().hdel(this.dbIndex(), this.key(), oldField);
            }
        }
    }

    @Override
    public boolean deleteRow() {
        try {
            long count = this.client().hdel(this.dbIndex(), this.key(), this.currentRow.getField());
            if (count > 0) {
                this.rows().remove(this.currentRow);
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return false;
    }

    @Override
    public void refreshKeyValue() {
        Map<String, String> value = this.client().hgetAll(this.dbIndex(), this.key());
        this.value.valueOfHash(value);
        // 清空未保存的数据
        this.clearData();
    }

    @Override
    public ShellRedisHashValue.RedisHashRow rawValue() {
        return this.currentRow;
    }

    @Override
    public boolean reloadRow() {
        String value = this.client().hget(this.dbIndex(), this.key(), this.currentRow.getField());
        if (value != null) {
            this.currentRow.setValue(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean isDataTooBig() {
        Object o = this.data();
        if (o instanceof ShellRedisHashValue.RedisHashRow r) {
            String s = r.getValue();
            if (s.length() > DATA_MAX) {
                return true;
            }
            if (s.lines().anyMatch(l -> l.length() > LINE_MAX) ) {
                return true;
            }
            String field = r.getField();
            if (field.length() > DATA_MAX) {
                return true;
            }
            return field.lines().anyMatch(l -> l.length() > LINE_MAX);
        }
        return false;
    }
}
