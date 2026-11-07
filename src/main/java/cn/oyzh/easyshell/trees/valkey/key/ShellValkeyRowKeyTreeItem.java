package cn.oyzh.easyshell.trees.valkey.key;

import cn.oyzh.easyshell.valkey.key.ShellValkeyKey;
import cn.oyzh.easyshell.valkey.key.ShellValkeyKeyRow;

import cn.oyzh.fx.plus.information.MessageBox;

import java.util.Collections;
import java.util.List;

/**
 * redis 行类型键节点
 *
 * @author oyzh
 * @since 2025-09-01
 */
public abstract class ShellValkeyRowKeyTreeItem<R extends ShellValkeyKeyRow> extends ShellValkeyKeyTreeItem {

    /**
     * 获取当前行
     *
     * @return 当前行
     */
    public R currentRow() {
        return currentRow;
    }

    /**
     * 设置当前行
     *
     * @param currentRow 当前行
     * @return 当前对象
     */
    public ShellValkeyRowKeyTreeItem<R> currentRow(R currentRow) {
        this.currentRow = currentRow;
        return this;
    }

    /**
     * 当前行
     */
    protected R currentRow;

    /**
     * 构造行类型键节点
     *
     * @param value  键对象
     * @param keysItem 键列表节点
     */
    public ShellValkeyRowKeyTreeItem(ShellValkeyKey value, ShellValkeyKeysTreeItem keysItem) {
        super(value, keysItem);
    }

    /**
     * 删除行
     *
     * @return 结果
     */
    public boolean deleteRow() {
        return false;
    }

    /**
     * 重载行
     *
     * @return 结果
     */
    public boolean reloadRow() {
        return false;
    }

    /**
     * 检查行是否存在
     *
     * @return 结果
     */
    public boolean checkRowExists() {
        return false;
    }

    /**
     * 获取行列表
     *
     * @return 行列表
     */
    public List<R> rows() {
        try {
            this.refreshKeyValue();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
        if (this.value.isSetKey()) {
            return (List<R>) this.value.asSetValue().getValue();
        }
        if (this.value.isZSetKey()) {
            return (List<R>) this.value.asZSetValue().getValue();
        }
        if (this.value.isListKey()) {
            return (List<R>) this.value.asListValue().getValue();
        }
        if (this.value.isHashKey()) {
            return (List<R>) this.value.asHashValue().getValue();
        }
        if (this.value.isStreamKey()) {
            return (List<R>) this.value.asStreamValue().getValue();
        }
        return Collections.emptyList();
    }

    /**
     * 是否选中行
     *
     * @return 是否选中行
     */
    public boolean isSelectRow() {
        return this.currentRow != null;
    }

    @Override
    public Object rawData() {
        return this.currentRow == null ? null : this.currentRow.getValue();
    }

    /**
     * 数据是否太大
     *
     * @return 结果
     */
    public boolean isDataTooBig() {
        return false;
    }
}
