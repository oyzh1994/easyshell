package cn.oyzh.easyshell.fx.oracle.routine;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * Oracle存储过程参数模式下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleParamModeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("IN");
        this.addItem("OUT");
        this.addItem("IN/OUT");
        super.initNode();
    }
}
