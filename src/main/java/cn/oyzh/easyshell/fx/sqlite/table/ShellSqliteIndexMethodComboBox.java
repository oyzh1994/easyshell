package cn.oyzh.easyshell.fx.sqlite.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * SQLite索引方法选择框。
 *
 * @author oyzh
 * @since 2026-11-03
 */
public class ShellSqliteIndexMethodComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("BTREE");
        super.initNode();
    }
}
