package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/8/27
 */
public class DataRecordLabelComboBox extends FXComboBox<String> {

    {
        this.addItem("(Root)");
        this.addItem("RECORDS");
    }

    public boolean isRoot() {
        return this.getSelectedIndex() == 0;
    }
}
