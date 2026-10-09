package cn.oyzh.easyshell.fx.mariadb;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MariaDB安全类型下拉框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbSecurityTypeComboBox extends FXComboBox<String> {

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
