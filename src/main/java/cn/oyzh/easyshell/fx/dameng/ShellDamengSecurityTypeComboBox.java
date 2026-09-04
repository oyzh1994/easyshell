package cn.oyzh.easyshell.fx.dameng;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db安全类型下拉框
 *
 * @author oyzh
 * @since 2024/08/07
 */
public class ShellDamengSecurityTypeComboBox extends FXComboBox<String> {

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        }
    }

    @Override
    public void initNode() {
        this.addItem("DEFINER");
        this.addItem("CURRENT_USER");
        super.initNode();
    }
}
