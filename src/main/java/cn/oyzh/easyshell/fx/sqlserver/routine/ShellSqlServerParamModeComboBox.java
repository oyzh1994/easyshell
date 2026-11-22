package cn.oyzh.easyshell.fx.sqlserver.routine;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * SqlServer存储过程参数模式下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerParamModeComboBox extends FXComboBox<String> {

    private final boolean inputOnly;

    public ShellSqlServerParamModeComboBox() {
        this(false);
    }

    public ShellSqlServerParamModeComboBox(boolean inputOnly) {
        this.inputOnly = inputOnly;
        this.getItems().clear();
        this.getItems().add("IN");
        if (!inputOnly) {
            this.getItems().add("OUT");
        }
    }

    @Override
    public void initNode() {
        super.initNode();
    }
}
