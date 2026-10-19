package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 行格式下拉框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbRowFormatComboBox extends FXComboBox<String> {

    @Override
    public void select(String rowFormat) {
        if (rowFormat != null) {
            super.select(rowFormat.toUpperCase());
        } else {
            super.clearSelection();
        }
    }

    @Override
    public void initNode() {
        this.addItem("COMPACT");
        this.addItem("COMPRESSED");
        this.addItem("DEFAULT");
        this.addItem("DYNAMIC");
        this.addItem("FIXED");
        this.addItem("REDUNDANT");
        super.initNode();
    }
}
