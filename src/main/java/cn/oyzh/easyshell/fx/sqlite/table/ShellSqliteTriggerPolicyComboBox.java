package cn.oyzh.easyshell.fx.sqlite.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * SQLite触发器策略选择框。
 *
 * @author oyzh
 * @since 2026-11-03
 */
public class ShellSqliteTriggerPolicyComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("BEFORE INSERT");
        this.addItem("BEFORE UPDATE");
        this.addItem("BEFORE DELETE");
        this.addItem("AFTER INSERT");
        this.addItem("AFTER UPDATE");
        this.addItem("AFTER DELETE");
        super.initNode();
    }
}
