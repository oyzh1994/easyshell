package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 外键策略选择框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbForeignKeyPolicyComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("CASCADE");
        this.addItem("NO ACTION");
        this.addItem("RESTRICT");
        this.addItem("SET NULL");
    }
}
