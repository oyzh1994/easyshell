package cn.oyzh.easyshell.trees.valkey.key;

import cn.oyzh.easyshell.valkey.key.ShellValkeyKey;
import cn.oyzh.easyshell.valkey.key.ShellValkeyListValue;

import cn.oyzh.fx.plus.information.MessageBox;

import java.util.List;

/**
 * redis list类型键节点
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyListKeyTreeItem extends ShellValkeyRowKeyTreeItem<ShellValkeyListValue.ValkeyListRow> {

    /**
     * 构造list类型键节点
     *
     * @param value  键对象
     * @param keysItem 键列表节点
     */
    public ShellValkeyListKeyTreeItem(ShellValkeyKey value, ShellValkeyKeysTreeItem keysItem) {
        super(value, keysItem);
    }

    @Override
    public ShellValkeyListValue.ValkeyListRow data() {
        return (ShellValkeyListValue.ValkeyListRow) super.data();
    }

    @Override
    public void data(Object data) {
        if (data instanceof ShellValkeyListValue.ValkeyListRow row) {
            super.data(row.clone());
        } else {
            super.clearData();
        }
    }

    @Override
    public ShellValkeyListValue.ValkeyListRow unsavedValue() {
        return (ShellValkeyListValue.ValkeyListRow) super.unsavedValue();
    }

    @Override
    public void saveKeyValue() {
        ShellValkeyListValue.ValkeyListRow row = this.data();
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
        // 更新数据
        if (value instanceof ShellValkeyListValue.ValkeyListRow row) {
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
            MessageBox.exception(ex, this.getWindow());
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
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public ShellValkeyListValue.ValkeyListRow rawValue() {
        return this.currentRow;
    }

    @Override
    public boolean isDataTooBig() {
        Object o = this.data();
        if (o instanceof ShellValkeyListValue.ValkeyListRow r) {
            String s = r.getValue();
            if (s.length() > DATA_MAX) {
                return true;
            }
            return s.lines().anyMatch(l -> l.length() > LINE_MAX);
        }
        return false;
    }
}
