package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db删除策略选择框
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class MysqlForeignKeyPolicyComboBox extends FXComboBox<String> {

    {
        this.addItem("CASCADE");
        this.addItem("NO ACTION");
        this.addItem("RESTRICT");
        this.addItem("SET NULL");
    }
}
