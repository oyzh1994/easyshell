package cn.oyzh.easyshell.fx.postgresql.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * PostgreSQL索引方法下拉选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlIndexMethodComboBox extends FXComboBox<String> {

    {
        this.addItem("");
        this.addItem("BTREE");
        this.addItem("HASH");
        this.addItem("GIN");
        this.addItem("GIST");
        this.addItem("BRIN");
        this.addItem("SPGIST");
    }
}
