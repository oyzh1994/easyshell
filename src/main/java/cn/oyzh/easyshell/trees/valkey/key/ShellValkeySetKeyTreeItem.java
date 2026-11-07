package cn.oyzh.easyshell.trees.valkey.key;

import cn.oyzh.easyshell.valkey.key.ShellValkeyKey;
import cn.oyzh.easyshell.valkey.key.ShellValkeySetValue;

import cn.oyzh.fx.plus.information.MessageBox;

import java.util.Objects;
import java.util.Set;

/**
 * redis set键树节点
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeySetKeyTreeItem extends ShellValkeyRowKeyTreeItem<ShellValkeySetValue.ValkeySetRow> {

    /**
     * 构造set类型键节点
     *
     * @param value  键对象
     * @param keysItem 键列表节点
     */
    public ShellValkeySetKeyTreeItem(ShellValkeyKey value, ShellValkeyKeysTreeItem keysItem) {
        super(value, keysItem);
    }

    @Override
    public ShellValkeySetValue.ValkeySetRow data() {
        return (ShellValkeySetValue.ValkeySetRow) super.data();
    }

    @Override
    public void data(Object data) {
        if (data instanceof ShellValkeySetValue.ValkeySetRow row) {
            super.data(row.clone());
        } else {
            super.clearData();
        }
    }

    @Override
    public ShellValkeySetValue.ValkeySetRow unsavedValue() {
        return (ShellValkeySetValue.ValkeySetRow) super.unsavedValue();
    }

    @Override
    public void saveKeyValue() {
        ShellValkeySetValue.ValkeySetRow row = this.data();
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
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    protected void setKeyValue(Object value) {
        if (value instanceof ShellValkeySetValue.ValkeySetRow row) {
            try {
                String rowValue = row.getValue();
                String currentRowValue = this.currentRow.getValue();
                // 删除当前成员
                if (!Objects.equals(rowValue, currentRowValue)) {
                    this.client().srem(this.dbIndex(), this.key(), currentRowValue);
                }
                // 添加成员
                this.client().sadd(this.dbIndex(), this.key(), rowValue);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        }
    }

    @Override
    public boolean deleteRow() {
        try {
            long count = this.client().srem(this.dbIndex(), this.key(), this.currentRow.getValue());
            if (count > 0) {
                this.rows().remove(this.currentRow);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
        return false;
    }

    @Override
    public void refreshKeyValue() {
        try {
            // 更新数据
            Set<String> value = this.client().smembers(this.dbIndex(), this.key());
            this.value.valueOfSet(value);
            // 清空未保存的数据
            this.clearData();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public ShellValkeySetValue.ValkeySetRow rawValue() {
        return this.currentRow;
    }

    @Override
    public boolean checkRowExists() {
        if (this.isDataUnsaved()) {
            String rowValue = this.data().getValue();
            if (!Objects.equals(this.currentRow.getValue(), rowValue)) {
                return this.client().sismember(this.dbIndex(), this.key(), rowValue);
            }
        }
        return false;
    }

    @Override
    public boolean isDataTooBig() {
        Object o = this.data();
        if (o instanceof ShellValkeySetValue.ValkeySetRow r) {
            String s = r.getValue();
            if (s.length() > DATA_MAX) {
                return true;
            }
            return s.lines().anyMatch(l -> l.length() > LINE_MAX);
        }
        return false;
    }
}
