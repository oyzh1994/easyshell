package cn.oyzh.easyshell.fx.oracle.record;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * Oracle自动递增类型下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleAoutoIncrementTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("SEQUENCE_TRIGGER");
        this.addItem("IDENTITY");
        super.initNode();
    }
}
