package cn.oyzh.easyshell.fx.dameng.record;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 达梦自动递增类型下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengAoutoIncrementTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("IDENTITY");
        this.addItem("AUTO_INCREMENT");
        super.initNode();
    }
}
