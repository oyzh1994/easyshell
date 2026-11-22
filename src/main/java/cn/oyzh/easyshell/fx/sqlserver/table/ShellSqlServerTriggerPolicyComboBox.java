package cn.oyzh.easyshell.fx.sqlserver.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * SqlServer触发器策略下拉选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerTriggerPolicyComboBox extends FXComboBox<String> {

    {
        this.addItem("AFTER INSERT");
        this.addItem("AFTER UPDATE");
        this.addItem("AFTER DELETE");
        this.addItem("INSTEAD OF INSERT");
        this.addItem("INSTEAD OF UPDATE");
        this.addItem("INSTEAD OF DELETE");
    }
}
