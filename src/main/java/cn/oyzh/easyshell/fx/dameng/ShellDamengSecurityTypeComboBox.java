package cn.oyzh.easyshell.fx.dameng;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 达梦数据库安全类型下拉框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengSecurityTypeComboBox extends FXComboBox<String> {

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        } else {
            super.clearSelection();
        }
    }

    @Override
    public void initNode() {
        this.addItem("DEFINER");
        this.addItem("CURRENT_USER");
        super.initNode();
    }
}
