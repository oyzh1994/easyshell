package cn.oyzh.easyshell.fx.sqlserver;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * SqlServer函数/过程安全性下拉框
 *
 * SQL Server使用WITH EXECUTE AS CALLER/OWNER/SELF。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerRoutineSecurityTypeComboBox extends FXComboBox<String> {

    {
        this.addItem("CALLER");
        this.addItem("OWNER");
        this.addItem("SELF");
    }

    @Override
    public void select(String obj) {
        if (obj != null) {
            String value = obj.toUpperCase();
            if ("INVOKER".equals(value)) {
                value = "CALLER";
            } else if ("DEFINER".equals(value)) {
                value = "OWNER";
            }
            super.select(value);
        } else {
            super.clearSelection();
        }
    }
}
