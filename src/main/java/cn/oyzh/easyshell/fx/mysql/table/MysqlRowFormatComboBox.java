package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 行格式下拉框
 *
 * @author oyzh
 * @since 2024/07/17
 */
public class MysqlRowFormatComboBox extends FXComboBox<String> {

    {
        this.addItem("COMPACT");
        this.addItem("COMPRESSED");
        this.addItem("DEFAULT");
        this.addItem("DYNAMIC");
        this.addItem("FIXED");
        this.addItem("REDUNDANT");
    }

    @Override
    public void select(String rowFormat) {
        if (rowFormat != null) {
            super.select(rowFormat.toUpperCase());
        }
    }
}
