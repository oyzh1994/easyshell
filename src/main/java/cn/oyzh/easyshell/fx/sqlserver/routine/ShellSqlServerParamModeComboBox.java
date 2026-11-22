package cn.oyzh.easyshell.fx.sqlserver.routine;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * SqlServer存储过程参数模式下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerParamModeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("IN");
        this.addItem("OUT");
        this.addItem("INOUT");
        super.initNode();
    }
}
