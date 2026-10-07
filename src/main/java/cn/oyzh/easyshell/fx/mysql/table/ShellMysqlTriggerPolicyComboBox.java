package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 触发器策略选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlTriggerPolicyComboBox extends FXComboBox<String> {

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
