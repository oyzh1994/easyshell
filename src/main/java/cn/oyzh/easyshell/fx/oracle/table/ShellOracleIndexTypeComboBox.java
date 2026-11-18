package cn.oyzh.easyshell.fx.oracle.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * Oracle索引类型下拉选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleIndexTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("NORMAL");
        this.addItem("UNIQUE");
    }
}
