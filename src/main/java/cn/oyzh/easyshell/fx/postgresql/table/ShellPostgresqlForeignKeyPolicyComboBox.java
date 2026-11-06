package cn.oyzh.easyshell.fx.postgresql.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * PostgreSQL外键删除策略下拉选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlForeignKeyPolicyComboBox extends FXComboBox<String> {

    {
        this.addItem("CASCADE");
        this.addItem("NO ACTION");
        this.addItem("RESTRICT");
        this.addItem("SET NULL");
        this.addItem("SET DEFAULT");
    }
}
