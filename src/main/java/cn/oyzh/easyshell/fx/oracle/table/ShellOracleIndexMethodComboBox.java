package cn.oyzh.easyshell.fx.oracle.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * Oracle索引方法下拉选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleIndexMethodComboBox extends FXComboBox<String> {

    {
        this.addItem("");
        this.addItem("BTREE");
        this.addItem("BITMAP");
    }
}
