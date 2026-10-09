package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 触发器策略选择框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTriggerPolicyComboBox extends FXComboBox<String> {

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
