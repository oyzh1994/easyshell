package cn.oyzh.easyshell.db.fx;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/8/27
 */
public class DBDataRecordLabelComboBox extends FXComboBox<String> {

    {
        this.addItem("(Root)");
        this.addItem("RECORDS");
    }

    public boolean isRoot() {
        return this.getSelectedIndex() == 0;
    }
}
