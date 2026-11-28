package cn.oyzh.easyshell.fx.h2.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * H2索引类型选择框。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2IndexTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("NORMAL");
        this.addItem("HASH");
        this.addItem("SPATIAL");
        super.initNode();
    }
}
