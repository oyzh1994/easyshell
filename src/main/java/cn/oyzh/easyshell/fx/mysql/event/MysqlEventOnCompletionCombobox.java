package cn.oyzh.easyshell.fx.mysql.event;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024-09-09
 */
public class MysqlEventOnCompletionCombobox extends FXComboBox<String> {

    {
        this.addItem("PRESERVE");
        this.addItem("NOT PRESERVE");
    }

    @Override
    public void select(String val) {
        if (val != null) {
            super.select(val.toUpperCase());
        }
    }
}
