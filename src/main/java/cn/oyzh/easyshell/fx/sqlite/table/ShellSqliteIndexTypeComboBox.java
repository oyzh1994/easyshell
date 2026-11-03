package cn.oyzh.easyshell.fx.sqlite.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * SQLite索引类型选择框。
 *
 * @author oyzh
 * @since 2026-11-03
 */
public class ShellSqliteIndexTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("NORMAL");
        this.addItem("UNIQUE");
        super.initNode();
    }
}
