package cn.oyzh.easyshell.fx.oracle.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * Oracle外键删除策略下拉选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleForeignKeyPolicyComboBox extends FXComboBox<String> {

    {
        this.addItem("CASCADE");
        this.addItem("NO ACTION");
//        this.addItem("RESTRICT");
        this.addItem("SET NULL");
    }
}
