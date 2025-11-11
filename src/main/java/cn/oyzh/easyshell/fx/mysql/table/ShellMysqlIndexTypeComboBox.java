package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db索引类型选择框
 *
 * @author oyzh
 * @since 2024/01/24
 */
public class ShellMysqlIndexTypeComboBox extends FXComboBox<String> {

    {
        this.addItem("NORMAL");
        this.addItem("UNIQUE");
        this.addItem("FULLTEXT");
        this.addItem("SPATIAL");
    }
}
