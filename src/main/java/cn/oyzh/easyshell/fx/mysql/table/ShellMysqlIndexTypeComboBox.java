package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db索引类型选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlIndexTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("NORMAL");
        this.addItem("UNIQUE");
        this.addItem("FULLTEXT");
        this.addItem("SPATIAL");
        super.initNode();
    }
}
