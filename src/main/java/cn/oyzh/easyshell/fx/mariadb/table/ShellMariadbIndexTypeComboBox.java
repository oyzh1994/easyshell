package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db索引类型选择框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbIndexTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("NORMAL");
        this.addItem("UNIQUE");
        this.addItem("FULLTEXT");
        this.addItem("SPATIAL");
        super.initNode();
    }
}
