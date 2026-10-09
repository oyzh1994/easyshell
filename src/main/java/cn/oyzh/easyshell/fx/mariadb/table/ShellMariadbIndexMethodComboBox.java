package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db索引方法选择框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbIndexMethodComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("");
        this.addItem("BTREE");
        this.addItem("HASH");
        super.initNode();
    }
}
