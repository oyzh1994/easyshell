package cn.oyzh.easyshell.fx.h2.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * H2索引方法选择框。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2IndexMethodComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("BTREE");
        super.initNode();
    }
}
