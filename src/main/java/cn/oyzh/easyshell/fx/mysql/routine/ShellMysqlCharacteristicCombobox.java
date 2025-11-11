package cn.oyzh.easyshell.fx.mysql.routine;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/08/09
 */
public class ShellMysqlCharacteristicCombobox extends FXComboBox<String> {

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
        }
    }
}
