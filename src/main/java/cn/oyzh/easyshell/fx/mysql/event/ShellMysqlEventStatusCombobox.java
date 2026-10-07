package cn.oyzh.easyshell.fx.mysql.event;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MySQL事件状态下拉框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlEventStatusCombobox extends FXComboBox<String> {

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
            } else if (StringUtil.equalsIgnoreCase(val, "SLAVE_SIDE_DISABLED")) {
                this.select(2);
            } else if (StringUtil.equalsIgnoreCase(val, "REPLICA_SIDE_DISABLED")) {
                this.select(2);
            } else {
                super.select(val.toUpperCase());
            }
        } else {
            super.clearSelection();
        }
    }

    /**
     * 判断当前选中状态是否与指定状态一致
     *
     * @param val 状态值
     * @return 是否一致
     */
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
