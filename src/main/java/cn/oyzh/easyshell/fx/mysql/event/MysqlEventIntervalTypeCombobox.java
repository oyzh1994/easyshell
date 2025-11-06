package cn.oyzh.easyshell.fx.mysql.event;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024-09-10
 */
public class MysqlEventIntervalTypeCombobox extends FXComboBox<String> {

    {
        this.addItem("YEAR");
        this.addItem("QUARTER");
        this.addItem("MONTH");
        this.addItem("DAY");
        this.addItem("HOUR");
        this.addItem("MINUTE");
        this.addItem("WEEK");
        this.addItem("SECOND");
        this.addItem("YEAR_MONTH");
        this.addItem("DAY_HOUR");
        this.addItem("DAY_MINUTE");
        this.addItem("DAY_SECOND");
        this.addItem("HOUR_MINUTE");
        this.addItem("HOUR_SECOND");
        this.addItem("MINUTE_SECOND");
    }

    @Override
    public void select(String val) {
        if (val != null) {
            super.select(val.toUpperCase());
        }
    }
}
