package cn.oyzh.easyshell.fx.mysql.event;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024-09-09
 */
public class MysqlEventStatusCombobox extends FXComboBox<String> {

    {
        this.addItem("ENABLE");
        this.addItem("DISABLE");
        this.addItem("DISABLE ON SLAVE");
    }

    @Override
    public void select(String val) {
        if (val != null) {
            if (StringUtil.equalsIgnoreCase(val, "ENABLED")) {
                this.selectFirst();
            } else if (StringUtil.equalsIgnoreCase(val, "DISABLED")) {
                this.select(1);
            } else if (StringUtil.equalsIgnoreCase(val, "SLAVESIDE_DISABLED")) {
                this.select(2);
            } else {
                super.select(val.toUpperCase());
            }
        }
    }

    public boolean isSameStatus(String val) {
        if (val == null) {
            return false;
        }
        if (StringUtil.equalsAnyIgnoreCase("ENABLE", "ENABLED") && this.getSelectedIndex() == 0) {
            return true;
        }
        if (StringUtil.equalsAnyIgnoreCase("DISABLE", "DISABLED") && this.getSelectedIndex() == 1) {
            return true;
        }
        if (StringUtil.equalsAnyIgnoreCase("DISABLE ON SLAVE", "SLAVESIDE_DISABLED") && this.getSelectedIndex() == 2) {
            return true;
        }
        return false;
    }
}
