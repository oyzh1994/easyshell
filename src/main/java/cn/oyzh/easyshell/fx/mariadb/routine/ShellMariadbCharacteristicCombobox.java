package cn.oyzh.easyshell.fx.mariadb.routine;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MariaDB存储程序特性下拉框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbCharacteristicCombobox extends FXComboBox<String> {

    {
        this.addItem("LANGUAGE SQL");
        this.addItem("CONTAINS SQL");
        this.addItem("DETERMINISTIC");
        this.addItem("NO SQL");
        this.addItem("READS SQL DATA");
        this.addItem("MODIFIES SQL DATA");
        this.addItem("SQL SECURITY DEFINER");
        this.addItem("SQL SECURITY INVOKER");
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
