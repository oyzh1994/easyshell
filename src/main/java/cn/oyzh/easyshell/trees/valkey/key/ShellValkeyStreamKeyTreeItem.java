package cn.oyzh.easyshell.trees.valkey.key;

import cn.oyzh.easyshell.valkey.key.ShellValkeyKey;
import cn.oyzh.easyshell.valkey.key.ShellValkeyStreamValue;

import cn.oyzh.fx.plus.information.MessageBox;
import io.valkey.resps.StreamEntry;

import java.util.List;

/**
 * redis stream类型键节点
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyStreamKeyTreeItem extends ShellValkeyRowKeyTreeItem<ShellValkeyStreamValue.ValkeyStreamRow> {

    /**
     * 构造stream类型键节点
     *
     * @param value  键对象
     * @param keysItem 键列表节点
     */
    public ShellValkeyStreamKeyTreeItem(ShellValkeyKey value, ShellValkeyKeysTreeItem keysItem) {
        super(value, keysItem);
    }

    @Override
    public boolean deleteRow() {
        try {
            long count = this.client().xdel(this.dbIndex(), this.key(), this.currentRow.getStreamId());
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
        List<StreamEntry> value = this.client().xrange(this.dbIndex(), this.key());
        this.value.valueOfStream(value);
        this.clearData();
    }

    @Override
    public ShellValkeyStreamValue.ValkeyStreamRow rawValue() {
        return this.currentRow;
    }
}
