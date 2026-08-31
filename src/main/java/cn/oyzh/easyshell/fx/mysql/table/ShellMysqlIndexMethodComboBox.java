package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db索引方法选择框
 *
 * @author oyzh
 * @since 2024/01/24
 */
public class ShellMysqlIndexMethodComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("");
        this.addItem("BTREE");
        this.addItem("HASH");
        super.initNode();
    }
}
