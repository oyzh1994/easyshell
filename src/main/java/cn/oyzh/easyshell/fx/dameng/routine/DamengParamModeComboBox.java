package cn.oyzh.easyshell.fx.dameng.routine;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 达梦存储过程参数模式下拉选择框
 *
 * @author oyzh
 * @since 2024/06/26
 */
public class DamengParamModeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("IN");
        this.addItem("OUT");
        this.addItem("IN/OUT");
    }
}
