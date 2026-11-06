package cn.oyzh.easyshell.fx.postgresql;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * PostgreSQL函数/过程安全性下拉框
 *
 * PostgreSQL 只有 SECURITY DEFINER / SECURITY INVOKER 两种状态，
 * 没有“未设置”的独立状态，默认即为 INVOKER。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlRoutineSecurityTypeComboBox extends FXComboBox<String> {

    {
        this.addItem("DEFINER");
        this.addItem("INVOKER");
    }

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        } else {
            super.clearSelection();
        }
    }
}
